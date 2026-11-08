package com.bonepeople.android.starvault.global.storage

import android.content.Context
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID

internal class VaultValidCopyStorage(context: Context) {
    private val copyDirectory = File(context.applicationContext.filesDir, DIRECTORY_NAME)

    fun stage(
        vaultId: String,
        sourceFile: File,
        operationStage: VaultOperationStage,
    ): StagedCopy {
        validateVaultId(vaultId, operationStage)
        val directory = requireCopyDirectory(operationStage)
        val stagedFile = try {
            File.createTempFile("$PENDING_PREFIX$vaultId-", FILE_SUFFIX, directory)
        } catch (exception: IOException) {
            throw VaultStorageException.IoFailure(operationStage, exception)
        } catch (exception: SecurityException) {
            throw VaultStorageException.IoFailure(operationStage, exception)
        }
        try {
            copyAndSync(sourceFile, stagedFile)
            if (!contentEquals(sourceFile, stagedFile)) {
                throw IOException("Staged valid copy does not match the verified document")
            }
            return StagedCopy(vaultId, stagedFile, operationStage)
        } catch (exception: IOException) {
            deleteAfterFailure(stagedFile, exception)
            throw VaultStorageException.IoFailure(operationStage, exception)
        } catch (exception: SecurityException) {
            deleteAfterFailure(stagedFile, exception)
            throw VaultStorageException.IoFailure(operationStage, exception)
        }
    }

    fun replace(stagedCopy: StagedCopy): Replacement {
        val operationStage = stagedCopy.operationStage
        val directory = requireCopyDirectory(operationStage)
        val stagedFile = stagedCopy.file
        if (stagedFile.parentFile != directory || !stagedFile.isFile) {
            throw VaultStorageException.IoFailure(
                operationStage,
                IOException("Staged valid copy is unavailable"),
            )
        }
        val targetFile = File(directory, stagedCopy.vaultId + FILE_SUFFIX)
        var backupFile: File? = null
        try {
            if (targetFile.exists()) {
                backupFile = createMoveTarget(
                    prefix = "$BACKUP_PREFIX${stagedCopy.vaultId}-",
                    directory = directory,
                )
                move(targetFile, backupFile)
            }
            move(stagedFile, targetFile)
            return Replacement(this, targetFile, backupFile, operationStage)
        } catch (exception: IOException) {
            restoreFailedReplacement(targetFile, backupFile, exception)
            throw VaultStorageException.IoFailure(operationStage, exception)
        } catch (exception: SecurityException) {
            restoreFailedReplacement(targetFile, backupFile, exception)
            throw VaultStorageException.IoFailure(operationStage, exception)
        }
    }

    private fun requireCopyDirectory(operationStage: VaultOperationStage): File {
        try {
            if (!copyDirectory.exists() && !copyDirectory.mkdirs()) {
                throw IOException("Valid-copy directory could not be created")
            }
            if (!copyDirectory.isDirectory) {
                throw IOException("Valid-copy path is not a directory")
            }
            return copyDirectory
        } catch (exception: IOException) {
            throw VaultStorageException.IoFailure(operationStage, exception)
        } catch (exception: SecurityException) {
            throw VaultStorageException.IoFailure(operationStage, exception)
        }
    }

    private fun validateVaultId(vaultId: String, operationStage: VaultOperationStage) {
        val valid = try {
            UUID.fromString(vaultId).toString() == vaultId
        } catch (_: IllegalArgumentException) {
            false
        }
        if (!valid) {
            throw VaultStorageException.InvalidFile(
                operationStage,
                IllegalArgumentException("Vault ID is invalid"),
            )
        }
    }

    private fun copyAndSync(sourceFile: File, targetFile: File) {
        BufferedInputStream(sourceFile.inputStream()).use { input ->
            FileOutputStream(targetFile).use { fileOutput ->
                val output = BufferedOutputStream(fileOutput)
                input.copyTo(output)
                output.flush()
                fileOutput.fd.sync()
            }
        }
    }

    private fun contentEquals(expectedFile: File, actualFile: File): Boolean {
        if (expectedFile.length() != actualFile.length()) return false
        return BufferedInputStream(expectedFile.inputStream()).use { expected ->
            BufferedInputStream(actualFile.inputStream()).use { actual ->
                streamsEqual(expected, actual)
            }
        }
    }

    private fun streamsEqual(expected: InputStream, actual: InputStream): Boolean {
        val expectedBuffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val actualBuffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val expectedCount = readChunk(expected, expectedBuffer)
            val actualCount = readChunk(actual, actualBuffer)
            if (expectedCount != actualCount) return false
            if (expectedCount == -1) return true
            for (index in 0 until expectedCount) {
                if (expectedBuffer[index] != actualBuffer[index]) return false
            }
        }
    }

    private fun readChunk(input: InputStream, buffer: ByteArray): Int {
        var byteCount = 0
        while (byteCount < buffer.size) {
            val readCount = input.read(buffer, byteCount, buffer.size - byteCount)
            if (readCount == -1) break
            if (readCount == 0) {
                val value = input.read()
                if (value == -1) break
                buffer[byteCount] = value.toByte()
                byteCount += 1
            } else {
                byteCount += readCount
            }
        }
        return if (byteCount == 0) -1 else byteCount
    }

    private fun createMoveTarget(prefix: String, directory: File): File {
        val target = File.createTempFile(prefix, TRANSACTION_SUFFIX, directory)
        if (!target.delete()) throw IOException("Transaction placeholder could not be deleted")
        return target
    }

    private fun move(source: File, target: File) {
        try {
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(source.toPath(), target.toPath())
        }
    }

    private fun restoreFailedReplacement(
        targetFile: File,
        backupFile: File?,
        failure: Throwable,
    ) {
        if (backupFile == null || !backupFile.exists()) return
        try {
            if (targetFile.exists() && !targetFile.delete()) {
                throw IOException("Failed valid copy could not be removed")
            }
            move(backupFile, targetFile)
        } catch (rollbackFailure: Throwable) {
            failure.addSuppressed(rollbackFailure)
        }
    }

    private fun rollback(
        targetFile: File,
        backupFile: File?,
        operationStage: VaultOperationStage,
    ): VaultStorageException.IoFailure? {
        return try {
            if (backupFile == null) {
                if (targetFile.exists() && !targetFile.delete()) {
                    throw IOException("New valid copy could not be rolled back")
                }
            } else if (backupFile.exists()) {
                val failedCopy = if (targetFile.exists()) {
                    createMoveTarget(ROLLBACK_PREFIX, copyDirectory).also { move(targetFile, it) }
                } else {
                    null
                }
                try {
                    move(backupFile, targetFile)
                } catch (exception: Throwable) {
                    if (failedCopy != null && failedCopy.exists() && !targetFile.exists()) {
                        try {
                            move(failedCopy, targetFile)
                        } catch (restoreFailure: Throwable) {
                            exception.addSuppressed(restoreFailure)
                        }
                    }
                    throw exception
                }
                if (failedCopy?.exists() == true && !failedCopy.delete()) {
                    throw IOException("Failed valid copy could not be deleted after rollback")
                }
            }
            null
        } catch (exception: IOException) {
            VaultStorageException.IoFailure(operationStage, exception)
        } catch (exception: SecurityException) {
            VaultStorageException.IoFailure(operationStage, exception)
        }
    }

    private fun complete(backupFile: File?) {
        try {
            if (backupFile?.exists() == true) backupFile.delete()
        } catch (_: SecurityException) {
            Unit
        }
    }

    private fun deleteAfterFailure(file: File, failure: Throwable) {
        try {
            if (file.exists() && !file.delete()) {
                failure.addSuppressed(IOException("Private staged copy could not be deleted"))
            }
        } catch (cleanupFailure: SecurityException) {
            failure.addSuppressed(cleanupFailure)
        }
    }

    internal class StagedCopy(
        val vaultId: String,
        val file: File,
        val operationStage: VaultOperationStage,
    ) {
        fun discard(): VaultStorageException.IoFailure? {
            return try {
                if (file.exists() && !file.delete()) {
                    VaultStorageException.IoFailure(
                        operationStage,
                        IOException("Private staged copy could not be deleted"),
                    )
                } else {
                    null
                }
            } catch (exception: SecurityException) {
                VaultStorageException.IoFailure(operationStage, exception)
            }
        }
    }

    internal class Replacement(
        private val storage: VaultValidCopyStorage,
        private val targetFile: File,
        private val backupFile: File?,
        private val operationStage: VaultOperationStage,
    ) {
        private var active = true

        fun rollback(): VaultStorageException.IoFailure? {
            if (!active) return null
            active = false
            return storage.rollback(targetFile, backupFile, operationStage)
        }

        fun complete() {
            if (!active) return
            active = false
            storage.complete(backupFile)
        }
    }

    companion object {
        private const val DIRECTORY_NAME = "vault-copies"
        private const val FILE_SUFFIX = ".starvault"
        private const val TRANSACTION_SUFFIX = ".transaction"
        private const val PENDING_PREFIX = ".pending-"
        private const val BACKUP_PREFIX = ".backup-"
        private const val ROLLBACK_PREFIX = ".rollback-"
    }
}