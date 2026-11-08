package com.bonepeople.android.starvault.global.storage

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream

class VaultDocumentStorage(context: Context) {
    private val contentResolver: ContentResolver = context.applicationContext.contentResolver
    private val temporaryDirectory: File = context.applicationContext.cacheDir

    fun read(uri: Uri, password: CharArray): VaultPayloadInfo {
        return readVerified(uri, password) { _, document, _ -> document.payload }
    }

    internal fun <T> readVerified(
        uri: Uri,
        password: CharArray,
        action: (encryptedFile: File, document: VaultDecodedDocumentInfo, fileKey: ByteArray) -> T,
    ): T {
        val stablePassword = password.copyOf()
        return try {
            withTemporaryFile(VaultOperationStage.OPEN, INPUT_PREFIX) { stableInput ->
                copyUriToPrivateFile(uri, stableInput, VaultOperationStage.OPEN)
                runCodec(VaultOperationStage.OPEN) {
                    VaultFileCodec.readVerified(stableInput, stablePassword) { document, fileKey ->
                        action(stableInput, document, fileKey)
                    }
                }
            }
        } finally {
            stablePassword.fill('\u0000')
        }
    }

    fun write(
        uri: Uri,
        payload: VaultPayloadInfo,
        password: CharArray,
    ): VaultFileHeaderInfo {
        return writeVerified(uri, payload, password) { _, document, _ -> document.header }
    }

    internal fun <T> writeVerified(
        uri: Uri,
        payload: VaultPayloadInfo,
        password: CharArray,
        action: (encryptedFile: File, document: VaultDecodedDocumentInfo, fileKey: ByteArray) -> T,
    ): T {
        val stablePassword = password.copyOf()
        return try {
            withTemporaryFile(VaultOperationStage.SAVE, CANDIDATE_PREFIX) { candidate ->
                runCodec(VaultOperationStage.SAVE) {
                    VaultFileCodec.write(payload, stablePassword, candidate)
                }
                val expectedPayload = runCodec(VaultOperationStage.VERIFY) {
                    VaultFileCodec.read(candidate, stablePassword)
                }
                copyPrivateFileToUri(candidate, uri)
                verifyWrittenDocument(uri, candidate, expectedPayload, stablePassword, action)
            }
        } finally {
            stablePassword.fill('\u0000')
        }
    }

    private fun <T> verifyWrittenDocument(
        uri: Uri,
        candidate: File,
        expectedPayload: VaultPayloadInfo,
        password: CharArray,
        action: (encryptedFile: File, document: VaultDecodedDocumentInfo, fileKey: ByteArray) -> T,
    ): T {
        return try {
            withTemporaryFile(VaultOperationStage.VERIFY, READ_BACK_PREFIX) { readBack ->
                copyUriToPrivateFile(uri, readBack, VaultOperationStage.VERIFY)
                if (!contentEquals(candidate, readBack)) {
                    throw VaultStorageException.IoFailure(
                        VaultOperationStage.VERIFY,
                        IOException("Written document does not match the encrypted candidate"),
                    )
                }
                runCodec(VaultOperationStage.VERIFY) {
                    VaultFileCodec.readVerified(readBack, password) { document, fileKey ->
                        if (document.payload != expectedPayload) {
                            throw VaultStorageException.IoFailure(
                                VaultOperationStage.VERIFY,
                                IOException("Written document does not match the candidate payload"),
                            )
                        }
                        action(readBack, document, fileKey)
                    }
                }
            }
        } catch (exception: VaultStorageException.IoFailure) {
            throw exception
        } catch (exception: VaultStorageException) {
            throw VaultStorageException.IoFailure(VaultOperationStage.VERIFY, exception)
        }
    }

    private fun copyUriToPrivateFile(
        uri: Uri,
        targetFile: File,
        stage: VaultOperationStage,
    ) {
        try {
            val source = contentResolver.openInputStream(uri)
                ?: throw IOException("Document provider returned no input stream")
            BufferedInputStream(source).use { input ->
                BufferedOutputStream(targetFile.outputStream()).use { output ->
                    copyWithinFileLimit(input, output, stage)
                }
            }
        } catch (exception: VaultStorageException) {
            throw exception
        } catch (exception: IOException) {
            throw VaultStorageException.IoFailure(stage, exception)
        } catch (exception: SecurityException) {
            throw VaultStorageException.IoFailure(stage, exception)
        } catch (exception: IllegalArgumentException) {
            throw VaultStorageException.IoFailure(stage, exception)
        } catch (exception: UnsupportedOperationException) {
            throw VaultStorageException.IoFailure(stage, exception)
        }
    }

    private fun copyPrivateFileToUri(sourceFile: File, uri: Uri) {
        try {
            val target = contentResolver.openOutputStream(uri, WRITE_MODE)
                ?: throw IOException("Document provider returned no output stream")
            BufferedInputStream(sourceFile.inputStream()).use { input ->
                BufferedOutputStream(target).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (exception: IOException) {
            throw VaultStorageException.IoFailure(VaultOperationStage.SAVE, exception)
        } catch (exception: SecurityException) {
            throw VaultStorageException.IoFailure(VaultOperationStage.SAVE, exception)
        } catch (exception: IllegalArgumentException) {
            throw VaultStorageException.IoFailure(VaultOperationStage.SAVE, exception)
        } catch (exception: UnsupportedOperationException) {
            throw VaultStorageException.IoFailure(VaultOperationStage.SAVE, exception)
        }
    }

    private fun copyWithinFileLimit(
        input: InputStream,
        output: BufferedOutputStream,
        stage: VaultOperationStage,
    ) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var byteCount = 0L
        while (true) {
            val readCount = input.read(buffer)
            if (readCount == -1) break
            if (readCount == 0) {
                val value = input.read()
                if (value == -1) break
                if (byteCount == VaultFileProtocol.MAX_FILE_SIZE) {
                    throw VaultStorageException.ResourceLimitExceeded(
                        stage,
                        VaultResourceLimit.FILE_SIZE,
                    )
                }
                output.write(value)
                byteCount += 1L
                continue
            }
            if (readCount > VaultFileProtocol.MAX_FILE_SIZE - byteCount) {
                throw VaultStorageException.ResourceLimitExceeded(
                    stage,
                    VaultResourceLimit.FILE_SIZE,
                )
            }
            output.write(buffer, 0, readCount)
            byteCount += readCount
        }
    }

    private fun contentEquals(expectedFile: File, actualFile: File): Boolean {
        try {
            if (expectedFile.length() != actualFile.length()) return false
            return BufferedInputStream(expectedFile.inputStream()).use { expected ->
                BufferedInputStream(actualFile.inputStream()).use { actual ->
                    streamsEqual(expected, actual)
                }
            }
        } catch (exception: IOException) {
            throw VaultStorageException.IoFailure(VaultOperationStage.VERIFY, exception)
        } catch (exception: SecurityException) {
            throw VaultStorageException.IoFailure(VaultOperationStage.VERIFY, exception)
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

    private inline fun <T> runCodec(stage: VaultOperationStage, action: () -> T): T {
        try {
            return action()
        } catch (exception: VaultCodecException.InvalidFile) {
            throw VaultStorageException.InvalidFile(stage, exception)
        } catch (exception: VaultCodecException.AuthenticationFailed) {
            throw VaultStorageException.AuthenticationFailed(stage, exception)
        } catch (exception: VaultCodecException.Unsupported) {
            throw VaultStorageException.Unsupported(stage, exception)
        } catch (exception: VaultCodecException.ResourceLimitExceeded) {
            throw VaultStorageException.ResourceLimitExceeded(stage, exception.limit, exception)
        } catch (exception: IOException) {
            throw VaultStorageException.IoFailure(stage, exception)
        } catch (exception: SecurityException) {
            throw VaultStorageException.IoFailure(stage, exception)
        }
    }

    private inline fun <T> withTemporaryFile(
        stage: VaultOperationStage,
        prefix: String,
        action: (File) -> T,
    ): T {
        val temporaryFile = try {
            File.createTempFile(prefix, FILE_SUFFIX, temporaryDirectory)
        } catch (exception: IOException) {
            throw VaultStorageException.IoFailure(stage, exception)
        } catch (exception: SecurityException) {
            throw VaultStorageException.IoFailure(stage, exception)
        }
        var failure: Throwable? = null
        try {
            return action(temporaryFile)
        } catch (throwable: Throwable) {
            failure = throwable
            throw throwable
        } finally {
            val cleanupFailure = deleteTemporaryFile(temporaryFile, stage)
            if (cleanupFailure != null) {
                val currentFailure = failure
                if (currentFailure == null) throw cleanupFailure
                currentFailure.addSuppressed(cleanupFailure)
            }
        }
    }

    private fun deleteTemporaryFile(
        temporaryFile: File,
        stage: VaultOperationStage,
    ): VaultStorageException.IoFailure? {
        return try {
            if (temporaryFile.exists() && !temporaryFile.delete()) {
                VaultStorageException.IoFailure(
                    stage,
                    IOException("Private temporary file could not be deleted"),
                )
            } else {
                null
            }
        } catch (exception: SecurityException) {
            VaultStorageException.IoFailure(stage, exception)
        }
    }

    companion object {
        private const val INPUT_PREFIX = "vault-input-"
        private const val CANDIDATE_PREFIX = "vault-candidate-"
        private const val READ_BACK_PREFIX = "vault-read-back-"
        private const val FILE_SUFFIX = ".starvault"
        private const val WRITE_MODE = "wt"
    }
}