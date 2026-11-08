package com.bonepeople.android.starvault.global.storage

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.bonepeople.android.starvault.global.VaultManager
import com.bonepeople.android.starvault.global.data.VaultInfo
import java.io.File
import java.io.IOException
import java.util.UUID

class VaultStorage(context: Context) {
    private val contentResolver: ContentResolver = context.applicationContext.contentResolver
    private val documentStorage = VaultDocumentStorage(context)
    private val recentMetadataStorage = VaultRecentMetadataStorage(context)
    private val validCopyStorage = VaultValidCopyStorage(context)

    fun create(
        uri: Uri,
        vaultName: String,
        password: CharArray,
    ): VaultStorageResultInfo {
        return synchronized(operationLock) {
            val stablePassword = password.copyOf()
            var preparedDocument: PreparedDocument? = null
            try {
                val payload = VaultPayloadInfo(
                    revision = VaultFileProtocol.INITIAL_REVISION,
                    vault = VaultInfo(
                        id = UUID.randomUUID().toString(),
                        name = vaultName.trim(),
                    ),
                )
                val document = prepareCreatedDocument(uri, payload, stablePassword)
                preparedDocument = document
                commitPreparedDocument(uri, document, VaultOperationStage.SAVE).also {
                    preparedDocument = null
                }
            } catch (throwable: Throwable) {
                preparedDocument?.close()?.let(throwable::addSuppressed)
                throw throwable
            } finally {
                stablePassword.fill('\u0000')
            }
        }
    }

    fun open(
        uri: Uri,
        password: CharArray,
    ): VaultStorageResultInfo {
        return synchronized(operationLock) {
            val stablePassword = password.copyOf()
            var preparedDocument: PreparedDocument? = null
            try {
                val document = prepareOpenedDocument(uri, stablePassword)
                preparedDocument = document
                commitPreparedDocument(uri, document, VaultOperationStage.OPEN).also {
                    preparedDocument = null
                }
            } catch (throwable: Throwable) {
                preparedDocument?.close()?.let(throwable::addSuppressed)
                throw throwable
            } finally {
                stablePassword.fill('\u0000')
            }
        }
    }

    private fun prepareCreatedDocument(
        uri: Uri,
        payload: VaultPayloadInfo,
        password: CharArray,
    ): PreparedDocument {
        var capturedDocument: PreparedDocument? = null
        return try {
            documentStorage.writeVerified(uri, payload, password) { encryptedFile, document, fileKey ->
                prepareDocument(uri, encryptedFile, document, fileKey, VaultOperationStage.SAVE).also {
                    capturedDocument = it
                }
            }.also {
                capturedDocument = null
            }
        } catch (throwable: Throwable) {
            capturedDocument?.close()?.let(throwable::addSuppressed)
            throw throwable
        }
    }

    private fun prepareOpenedDocument(
        uri: Uri,
        password: CharArray,
    ): PreparedDocument {
        var capturedDocument: PreparedDocument? = null
        return try {
            documentStorage.readVerified(uri, password) { encryptedFile, document, fileKey ->
                prepareDocument(uri, encryptedFile, document, fileKey, VaultOperationStage.OPEN).also {
                    capturedDocument = it
                }
            }.also {
                capturedDocument = null
            }
        } catch (throwable: Throwable) {
            capturedDocument?.close()?.let(throwable::addSuppressed)
            throw throwable
        }
    }

    private fun prepareDocument(
        uri: Uri,
        encryptedFile: File,
        document: VaultDecodedDocumentInfo,
        fileKey: ByteArray,
        operationStage: VaultOperationStage,
    ): PreparedDocument {
        val stagedCopy = validCopyStorage.stage(
            vaultId = document.payload.vault.id,
            sourceFile = encryptedFile,
            operationStage = operationStage,
        )
        return try {
            val session = VaultSessionInfo(
                uri = uri,
                payload = document.payload,
                keyMaterial = VaultSessionKeyMaterial(document.header.salt, fileKey),
            )
            PreparedDocument(session, stagedCopy)
        } catch (throwable: Throwable) {
            stagedCopy.discard()?.let(throwable::addSuppressed)
            throw throwable
        }
    }

    private fun commitPreparedDocument(
        uri: Uri,
        preparedDocument: PreparedDocument,
        operationStage: VaultOperationStage,
    ): VaultStorageResultInfo {
        val displayName = queryDisplayName(uri, operationStage)
        val permissionChange = persistUriPermission(uri, operationStage)
        val replacement = try {
            validCopyStorage.replace(preparedDocument.stagedCopy)
        } catch (throwable: Throwable) {
            permissionChange.rollback()?.let(throwable::addSuppressed)
            throw throwable
        }
        val session = preparedDocument.session
        val metadata = VaultRecentMetadataInfo(
            uri = uri,
            vaultId = session.vaultId,
            displayName = displayName,
            revision = session.revision,
        )
        try {
            recentMetadataStorage.save(metadata)
        } catch (exception: IOException) {
            val failure = VaultStorageException.IoFailure(operationStage, exception)
            replacement.rollback()?.let(failure::addSuppressed)
            permissionChange.rollback()?.let(failure::addSuppressed)
            throw failure
        } catch (exception: SecurityException) {
            val failure = VaultStorageException.IoFailure(operationStage, exception)
            replacement.rollback()?.let(failure::addSuppressed)
            permissionChange.rollback()?.let(failure::addSuppressed)
            throw failure
        } catch (exception: RuntimeException) {
            val failure = VaultStorageException.IoFailure(operationStage, exception)
            replacement.rollback()?.let(failure::addSuppressed)
            permissionChange.rollback()?.let(failure::addSuppressed)
            throw failure
        }
        VaultManager.establishSession(session)
        preparedDocument.releaseSession()
        replacement.complete()
        return VaultStorageResultInfo(
            session = session,
            displayName = displayName,
            vaultId = session.vaultId,
            revision = session.revision,
        )
    }

    private fun queryDisplayName(uri: Uri, operationStage: VaultOperationStage): String {
        try {
            val cursor = contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null,
            ) ?: throw IOException("Document provider returned no metadata cursor")
            cursor.use {
                if (!it.moveToFirst()) throw IOException("Document metadata is empty")
                val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnIndex < 0) throw IOException("Document display name is unavailable")
                return it.getString(columnIndex)?.takeIf { value -> value.isNotEmpty() }
                    ?: throw IOException("Document display name is empty")
            }
        } catch (exception: IOException) {
            throw VaultStorageException.IoFailure(operationStage, exception)
        } catch (exception: SecurityException) {
            throw VaultStorageException.IoFailure(operationStage, exception)
        } catch (exception: IllegalArgumentException) {
            throw VaultStorageException.IoFailure(operationStage, exception)
        } catch (exception: UnsupportedOperationException) {
            throw VaultStorageException.IoFailure(operationStage, exception)
        } catch (exception: RuntimeException) {
            throw VaultStorageException.IoFailure(operationStage, exception)
        }
    }

    private fun persistUriPermission(
        uri: Uri,
        operationStage: VaultOperationStage,
    ): PersistedPermissionChange {
        val previousFlags = persistedPermissionFlags(uri, operationStage)
        try {
            takePersistableUriPermission(uri, READ_WRITE_URI_PERMISSION_FLAGS, operationStage)
        } catch (readWriteFailure: VaultStorageException.IoFailure) {
            if (operationStage != VaultOperationStage.OPEN) throw readWriteFailure
            try {
                takePersistableUriPermission(uri, READ_URI_PERMISSION_FLAG, operationStage)
            } catch (readFailure: VaultStorageException.IoFailure) {
                readFailure.addSuppressed(readWriteFailure)
                throw readFailure
            }
        }
        val currentFlags = persistedPermissionFlags(uri, operationStage)
        val newlyPersistedFlags = currentFlags and previousFlags.inv() and READ_WRITE_URI_PERMISSION_FLAGS
        val requiredFlags = if (operationStage == VaultOperationStage.OPEN) {
            READ_URI_PERMISSION_FLAG
        } else {
            READ_WRITE_URI_PERMISSION_FLAGS
        }
        if (currentFlags and requiredFlags != requiredFlags) {
            val failure = VaultStorageException.IoFailure(
                operationStage,
                IOException("Document permission was not persisted"),
            )
            PersistedPermissionChange(uri, newlyPersistedFlags, operationStage).rollback()
                ?.let(failure::addSuppressed)
            throw failure
        }
        return PersistedPermissionChange(uri, newlyPersistedFlags, operationStage)
    }

    private fun takePersistableUriPermission(
        uri: Uri,
        flags: Int,
        operationStage: VaultOperationStage,
    ) {
        try {
            contentResolver.takePersistableUriPermission(uri, flags)
        } catch (exception: SecurityException) {
            throw VaultStorageException.IoFailure(operationStage, exception)
        } catch (exception: IllegalArgumentException) {
            throw VaultStorageException.IoFailure(operationStage, exception)
        } catch (exception: UnsupportedOperationException) {
            throw VaultStorageException.IoFailure(operationStage, exception)
        } catch (exception: RuntimeException) {
            throw VaultStorageException.IoFailure(operationStage, exception)
        }
    }

    private fun persistedPermissionFlags(uri: Uri, operationStage: VaultOperationStage): Int {
        try {
            var flags = 0
            contentResolver.persistedUriPermissions
                .filter { it.uri == uri }
                .forEach { permission ->
                    if (permission.isReadPermission) {
                        flags = flags or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    if (permission.isWritePermission) {
                        flags = flags or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    }
                }
            return flags
        } catch (exception: SecurityException) {
            throw VaultStorageException.IoFailure(operationStage, exception)
        } catch (exception: RuntimeException) {
            throw VaultStorageException.IoFailure(operationStage, exception)
        }
    }

    private inner class PersistedPermissionChange(
        private val uri: Uri,
        private val flags: Int,
        private val operationStage: VaultOperationStage,
    ) {
        fun rollback(): VaultStorageException.IoFailure? {
            if (flags == 0) return null
            return try {
                contentResolver.releasePersistableUriPermission(uri, flags)
                null
            } catch (exception: SecurityException) {
                VaultStorageException.IoFailure(operationStage, exception)
            } catch (exception: IllegalArgumentException) {
                VaultStorageException.IoFailure(operationStage, exception)
            } catch (exception: UnsupportedOperationException) {
                VaultStorageException.IoFailure(operationStage, exception)
            } catch (exception: RuntimeException) {
                VaultStorageException.IoFailure(operationStage, exception)
            }
        }
    }

    private class PreparedDocument(
        val session: VaultSessionInfo,
        val stagedCopy: VaultValidCopyStorage.StagedCopy,
    ) {
        private var ownsSession = true

        fun releaseSession() {
            ownsSession = false
        }

        fun close(): VaultStorageException.IoFailure? {
            if (ownsSession) session.close()
            return stagedCopy.discard()
        }
    }

    companion object {
        private val operationLock = Any()
        private const val READ_URI_PERMISSION_FLAG = Intent.FLAG_GRANT_READ_URI_PERMISSION
        private val READ_WRITE_URI_PERMISSION_FLAGS =
            READ_URI_PERMISSION_FLAG or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    }
}