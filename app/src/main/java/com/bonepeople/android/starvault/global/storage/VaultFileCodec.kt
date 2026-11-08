package com.bonepeople.android.starvault.global.storage

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.EOFException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipException
import javax.crypto.AEADBadTagException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream

object VaultFileCodec {
    private val secureRandom = SecureRandom()

    fun write(
        payload: VaultPayloadInfo,
        password: CharArray,
        targetFile: File,
    ): VaultFileHeaderInfo {
        val salt = ByteArray(VaultFileProtocol.SALT_SIZE).also(secureRandom::nextBytes)
        val nonce = ByteArray(VaultFileProtocol.NONCE_SIZE).also(secureRandom::nextBytes)
        return write(payload, password, targetFile, salt, nonce)
    }

    fun read(sourceFile: File, password: CharArray): VaultPayloadInfo {
        return readVerified(sourceFile, password) { document, _ -> document.payload }
    }

    internal fun <T> readVerified(
        sourceFile: File,
        password: CharArray,
        action: (document: VaultDecodedDocumentInfo, fileKey: ByteArray) -> T,
    ): T {
        val totalFileLength = sourceFile.length()
        val header = readHeader(sourceFile, totalFileLength)
        val key = VaultCryptography.deriveKey(password, header.salt)
        return readVerified(sourceFile, totalFileLength, header, key, null, action)
    }

    internal fun <T> readVerifiedWithKey(
        sourceFile: File,
        expectedSalt: ByteArray,
        fileKey: ByteArray,
        action: (document: VaultDecodedDocumentInfo, fileKey: ByteArray) -> T,
    ): T {
        val key = fileKey.copyOf()
        return try {
            val totalFileLength = sourceFile.length()
            val header = readHeader(sourceFile, totalFileLength)
            readVerified(sourceFile, totalFileLength, header, key, expectedSalt, action)
        } catch (throwable: Throwable) {
            key.fill(0)
            throw throwable
        }
    }

    internal fun write(
        payload: VaultPayloadInfo,
        password: CharArray,
        targetFile: File,
        salt: ByteArray,
        nonce: ByteArray,
    ): VaultFileHeaderInfo {
        val key = VaultCryptography.deriveKey(password, salt)
        return try {
            writeEncrypted(payload, targetFile, salt, nonce, key)
        } finally {
            key.fill(0)
        }
    }

    internal fun writeWithKey(
        payload: VaultPayloadInfo,
        targetFile: File,
        salt: ByteArray,
        fileKey: ByteArray,
    ): VaultFileHeaderInfo {
        val nonce = ByteArray(VaultFileProtocol.NONCE_SIZE).also(secureRandom::nextBytes)
        val key = fileKey.copyOf()
        return try {
            writeEncrypted(payload, targetFile, salt, nonce, key)
        } finally {
            key.fill(0)
        }
    }

    private fun writeEncrypted(
        payload: VaultPayloadInfo,
        targetFile: File,
        salt: ByteArray,
        nonce: ByteArray,
        key: ByteArray,
    ): VaultFileHeaderInfo {
        val snapshot = immutableSnapshot(payload)
        val payloadLength = countCompressedLength(snapshot)
        val header = VaultFileHeaderInfo(
            vaultId = snapshot.vault.id,
            salt = salt.copyOf(),
            nonce = nonce.copyOf(),
            payloadLength = payloadLength,
        )
        val authenticatedHeader = VaultFileProtocol.encodeHeader(header)
        BufferedOutputStream(targetFile.outputStream()).use { fileOutput ->
            fileOutput.write(authenticatedHeader)
            val cipher = VaultCryptography.createCipher(
                mode = Cipher.ENCRYPT_MODE,
                key = key,
                nonce = header.nonce,
                authenticatedHeader = authenticatedHeader,
            )
            val compressedCounter = CountingOutputStream(
                CipherOutputStream(NonClosingOutputStream(fileOutput), cipher),
            )
            GZIPOutputStream(compressedCounter).use { gzipOutput ->
                VaultPayloadCodec.write(snapshot, gzipOutput)
            }
            if (compressedCounter.byteCount != payloadLength) {
                throw VaultCodecException.InvalidFile("Compressed payload length changed during encoding")
            }
        }
        val expectedFileLength = VaultFileProtocol.HEADER_SIZE + payloadLength +
            VaultFileProtocol.AUTHENTICATION_TAG_SIZE
        if (targetFile.length() != expectedFileLength) {
            throw VaultCodecException.InvalidFile("Encrypted candidate length is invalid")
        }
        return header
    }

    private fun <T> readVerified(
        sourceFile: File,
        totalFileLength: Long,
        header: VaultFileHeaderInfo,
        key: ByteArray,
        expectedSalt: ByteArray?,
        action: (document: VaultDecodedDocumentInfo, fileKey: ByteArray) -> T,
    ): T {
        val authenticatedHeader = VaultFileProtocol.encodeHeader(header)
        return try {
            authenticate(sourceFile, totalFileLength, header, authenticatedHeader, key)
            if (expectedSalt != null && !header.salt.contentEquals(expectedSalt)) {
                throw VaultCodecException.InvalidFile("File salt does not match the active session")
            }
            val payload = decode(sourceFile, totalFileLength, header, authenticatedHeader, key)
            action(VaultDecodedDocumentInfo(header, payload), key)
        } finally {
            key.fill(0)
        }
    }

    private fun readHeader(sourceFile: File, totalFileLength: Long): VaultFileHeaderInfo {
        return BufferedInputStream(sourceFile.inputStream()).use { input ->
            VaultFileProtocol.readHeader(input, totalFileLength)
        }
    }

    private fun authenticate(
        sourceFile: File,
        totalFileLength: Long,
        expectedHeader: VaultFileHeaderInfo,
        authenticatedHeader: ByteArray,
        key: ByteArray,
    ) {
        try {
            BufferedInputStream(sourceFile.inputStream()).use { fileInput ->
                val actualHeader = VaultFileProtocol.readHeader(fileInput, totalFileLength)
                ensureStableHeader(expectedHeader, actualHeader)
                val cipher = VaultCryptography.createCipher(
                    mode = Cipher.DECRYPT_MODE,
                    key = key,
                    nonce = actualHeader.nonce,
                    authenticatedHeader = authenticatedHeader,
                )
                AuthenticatedCipherInputStream(fileInput, cipher).use { decryptedInput ->
                    discard(decryptedInput)
                }
            }
        } catch (exception: VaultCodecException) {
            throw exception
        } catch (exception: java.io.IOException) {
            if (isAuthenticationFailure(exception)) {
                throw VaultCodecException.AuthenticationFailed(exception)
            }
            throw exception
        }
    }

    private fun decode(
        sourceFile: File,
        totalFileLength: Long,
        expectedHeader: VaultFileHeaderInfo,
        authenticatedHeader: ByteArray,
        key: ByteArray,
    ): VaultPayloadInfo {
        try {
            return BufferedInputStream(sourceFile.inputStream()).use { fileInput ->
                val actualHeader = VaultFileProtocol.readHeader(fileInput, totalFileLength)
                ensureStableHeader(expectedHeader, actualHeader)
                val cipher = VaultCryptography.createCipher(
                    mode = Cipher.DECRYPT_MODE,
                    key = key,
                    nonce = actualHeader.nonce,
                    authenticatedHeader = authenticatedHeader,
                )
                AuthenticatedCipherInputStream(fileInput, cipher).use { decryptedInput ->
                    GZIPInputStream(decryptedInput).use { gzipInput ->
                        val payload = VaultPayloadCodec.read(gzipInput, expectedHeader.vaultId)
                        if (gzipInput.read() != -1) {
                            throw VaultCodecException.InvalidFile("Compressed payload contains trailing data")
                        }
                        decryptedInput.finishAuthentication()
                        payload
                    }
                }
            }
        } catch (exception: VaultCodecException) {
            throw exception
        } catch (exception: ZipException) {
            throw VaultCodecException.InvalidFile("Compressed payload is invalid", exception)
        } catch (exception: EOFException) {
            throw VaultCodecException.InvalidFile("Compressed payload is truncated", exception)
        } catch (exception: java.io.IOException) {
            if (isAuthenticationFailure(exception)) {
                throw VaultCodecException.AuthenticationFailed(exception)
            }
            throw exception
        }
    }

    private fun countCompressedLength(payload: VaultPayloadInfo): Long {
        val compressedCounter = CountingOutputStream(DiscardingOutputStream)
        GZIPOutputStream(compressedCounter).use { gzipOutput ->
            VaultPayloadCodec.write(payload, gzipOutput)
        }
        return compressedCounter.byteCount
    }

    private fun immutableSnapshot(payload: VaultPayloadInfo): VaultPayloadInfo {
        return payload.copy(
            vault = payload.vault.copy(
                fieldDefinitionList = payload.vault.fieldDefinitionList.map { definition -> definition.copy() },
                tagDefinitionList = payload.vault.tagDefinitionList.map { definition -> definition.copy() },
                recordList = payload.vault.recordList.map { record ->
                    record.copy(
                        fieldList = record.fieldList.map { field ->
                            field.copy(
                                snapshotList = field.snapshotList.map { snapshot -> snapshot.copy() },
                            )
                        },
                        tagDefinitionIdList = record.tagDefinitionIdList.toList(),
                    )
                },
            ),
        )
    }

    private fun ensureStableHeader(expected: VaultFileHeaderInfo, actual: VaultFileHeaderInfo) {
        if (expected != actual) {
            throw VaultCodecException.InvalidFile("File header changed while decoding")
        }
    }

    private fun discard(input: InputStream) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (input.read(buffer) != -1) {
            Unit
        }
    }

    private fun isAuthenticationFailure(throwable: Throwable): Boolean {
        var current: Throwable? = throwable
        while (current != null) {
            if (current is AEADBadTagException || current is BadPaddingException) return true
            current = current.cause
        }
        return false
    }

    private class CountingOutputStream(
        private val target: OutputStream,
    ) : OutputStream() {
        var byteCount = 0L
            private set

        override fun write(value: Int) {
            target.write(value)
            byteCount += 1L
        }

        override fun write(buffer: ByteArray, offset: Int, length: Int) {
            target.write(buffer, offset, length)
            byteCount += length
        }

        override fun flush() {
            target.flush()
        }

        override fun close() {
            target.close()
        }
    }

    private class NonClosingOutputStream(
        private val target: OutputStream,
    ) : OutputStream() {
        override fun write(value: Int) {
            target.write(value)
        }

        override fun write(buffer: ByteArray, offset: Int, length: Int) {
            target.write(buffer, offset, length)
        }

        override fun flush() {
            target.flush()
        }
    }

    private class AuthenticatedCipherInputStream(
        private val source: InputStream,
        private val cipher: Cipher,
    ) : InputStream() {
        private val encryptedBuffer = ByteArray(DEFAULT_BUFFER_SIZE)
        private val singleByte = ByteArray(1)
        private var decryptedBuffer = ByteArray(0)
        private var decryptedOffset = 0
        private var finalized = false

        override fun read(): Int {
            val readCount = read(singleByte, 0, 1)
            return if (readCount == -1) -1 else singleByte[0].toInt() and 0xff
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            if (length == 0) return 0
            if (!ensureDecryptedData()) return -1
            val readCount = minOf(length, decryptedBuffer.size - decryptedOffset)
            decryptedBuffer.copyInto(
                destination = buffer,
                destinationOffset = offset,
                startIndex = decryptedOffset,
                endIndex = decryptedOffset + readCount,
            )
            decryptedOffset += readCount
            return readCount
        }

        override fun close() {
            source.close()
        }

        override fun available(): Int {
            return decryptedBuffer.size - decryptedOffset
        }

        fun finishAuthentication() {
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var foundTrailingData = false
            while (read(buffer) != -1) {
                foundTrailingData = true
            }
            if (foundTrailingData) {
                throw VaultCodecException.InvalidFile("Compressed payload contains trailing data")
            }
        }

        private fun ensureDecryptedData(): Boolean {
            if (decryptedOffset < decryptedBuffer.size) return true
            decryptedBuffer = ByteArray(0)
            decryptedOffset = 0
            while (!finalized) {
                val readCount = source.read(encryptedBuffer)
                decryptedBuffer = if (readCount == -1) {
                    finalized = true
                    finishCipher()
                } else if (readCount > 0) {
                    cipher.update(encryptedBuffer, 0, readCount) ?: ByteArray(0)
                } else {
                    ByteArray(0)
                }
                if (decryptedBuffer.isNotEmpty()) return true
            }
            return false
        }

        private fun finishCipher(): ByteArray {
            return try {
                cipher.doFinal()
            } catch (exception: GeneralSecurityException) {
                throw IOException("Authenticated decryption failed", exception)
            }
        }
    }

    private object DiscardingOutputStream : OutputStream() {
        override fun write(value: Int) = Unit

        override fun write(buffer: ByteArray, offset: Int, length: Int) = Unit
    }
}