package com.bonepeople.android.starvault.global.storage

import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

object VaultFileProtocol {
    const val CONTAINER_VERSION = 1
    const val SCHEMA_VERSION = 1
    const val INITIAL_REVISION = 1L

    const val KDF_ID_ARGON2ID = 1
    const val ARGON2_VERSION = 0x13
    const val ARGON2_MEMORY_KIB = 65_536
    const val ARGON2_ITERATIONS = 3
    const val ARGON2_PARALLELISM = 4
    const val SALT_SIZE = 16
    const val FILE_KEY_SIZE = 32

    const val CIPHER_ID_AES_256_GCM = 1
    const val NONCE_SIZE = 12
    const val AUTHENTICATION_TAG_SIZE = 16

    const val HEADER_SIZE = 88
    const val MAX_FILE_SIZE = 16L * 1024L * 1024L
    const val MAX_JSON_SIZE = 16L * 1024L * 1024L
    const val MAX_JSON_STRING_SIZE = 64L * 1024L
    const val MAX_JSON_ARRAY_ELEMENTS = 100_000L

    private val magic = byteArrayOf(
        0x53,
        0x54,
        0x41,
        0x52,
        0x56,
        0x4C,
        0x54,
        0x00,
    )

    fun writeHeader(header: VaultFileHeaderInfo, output: OutputStream) {
        validateHeader(header)
        val uuid = parseCanonicalUuid(header.vaultId)
        val dataOutput = DataOutputStream(output)
        dataOutput.write(magic)
        dataOutput.writeInt(header.containerVersion)
        dataOutput.writeLong(uuid.mostSignificantBits)
        dataOutput.writeLong(uuid.leastSignificantBits)
        dataOutput.writeInt(header.kdfId)
        dataOutput.writeInt(header.argon2Version)
        dataOutput.writeInt(header.argon2MemoryKib)
        dataOutput.writeInt(header.argon2Iterations)
        dataOutput.writeInt(header.argon2Parallelism)
        dataOutput.write(header.salt)
        dataOutput.writeInt(header.cipherId)
        dataOutput.write(header.nonce)
        dataOutput.writeLong(header.payloadLength)
    }

    internal fun encodeHeader(header: VaultFileHeaderInfo): ByteArray {
        return ByteArrayOutputStream(HEADER_SIZE).also { output ->
            writeHeader(header, output)
        }.toByteArray()
    }

    fun readHeader(input: InputStream, totalFileLength: Long): VaultFileHeaderInfo {
        validateFileLength(totalFileLength)
        val header = readHeaderFields(input)
        val expectedFileLength = expectedFileLength(header.payloadLength)
        if (expectedFileLength != totalFileLength) {
            throw VaultCodecException.InvalidFile("File length does not match payload length")
        }
        return header
    }

    fun validateContainer(input: InputStream): VaultFileHeaderInfo {
        val limitedInput = FileSizeLimitedInputStream(input)
        val header = readHeaderFields(limitedInput)
        consumeExactly(limitedInput, header.payloadLength)
        consumeExactly(limitedInput, AUTHENTICATION_TAG_SIZE.toLong())
        if (consumeRemainder(limitedInput)) {
            throw VaultCodecException.InvalidFile("File contains trailing data")
        }
        return header
    }

    internal fun parseCanonicalUuid(value: String): UUID {
        if (!CANONICAL_UUID_REGEX.matches(value)) {
            throw VaultCodecException.InvalidFile("ID is not a canonical lowercase UUID")
        }
        return try {
            UUID.fromString(value)
        } catch (exception: IllegalArgumentException) {
            throw VaultCodecException.InvalidFile("ID is not a valid UUID", exception)
        }
    }

    private fun readHeaderFields(input: InputStream): VaultFileHeaderInfo {
        val dataInput = DataInputStream(input)
        return try {
            val actualMagic = ByteArray(magic.size)
            dataInput.readFully(actualMagic)
            if (!actualMagic.contentEquals(magic)) {
                throw VaultCodecException.InvalidFile("File magic is invalid")
            }

            val containerVersion = dataInput.readInt()
            if (containerVersion != CONTAINER_VERSION) {
                throw VaultCodecException.Unsupported("Unsupported container version: $containerVersion")
            }

            val vaultId = UUID(dataInput.readLong(), dataInput.readLong()).toString()
            val header = VaultFileHeaderInfo(
                containerVersion = containerVersion,
                vaultId = vaultId,
                kdfId = dataInput.readInt(),
                argon2Version = dataInput.readInt(),
                argon2MemoryKib = dataInput.readInt(),
                argon2Iterations = dataInput.readInt(),
                argon2Parallelism = dataInput.readInt(),
                salt = ByteArray(SALT_SIZE).also(dataInput::readFully),
                cipherId = dataInput.readInt(),
                nonce = ByteArray(NONCE_SIZE).also(dataInput::readFully),
                payloadLength = dataInput.readLong(),
            )
            validateHeader(header)
            header
        } catch (exception: EOFException) {
            throw VaultCodecException.InvalidFile("File header is truncated", exception)
        }
    }

    private fun validateHeader(header: VaultFileHeaderInfo) {
        if (header.containerVersion != CONTAINER_VERSION) {
            throw VaultCodecException.Unsupported("Unsupported container version: ${header.containerVersion}")
        }
        parseCanonicalUuid(header.vaultId)
        if (
            header.kdfId != KDF_ID_ARGON2ID ||
            header.argon2Version != ARGON2_VERSION ||
            header.argon2MemoryKib != ARGON2_MEMORY_KIB ||
            header.argon2Iterations != ARGON2_ITERATIONS ||
            header.argon2Parallelism != ARGON2_PARALLELISM
        ) {
            throw VaultCodecException.Unsupported("Unsupported KDF parameters")
        }
        if (header.salt.size != SALT_SIZE) {
            throw VaultCodecException.InvalidFile("Salt length is invalid")
        }
        if (header.cipherId != CIPHER_ID_AES_256_GCM) {
            throw VaultCodecException.Unsupported("Unsupported cipher")
        }
        if (header.nonce.size != NONCE_SIZE) {
            throw VaultCodecException.InvalidFile("Nonce length is invalid")
        }
        expectedFileLength(header.payloadLength)
    }

    private fun validateFileLength(totalFileLength: Long) {
        if (totalFileLength > MAX_FILE_SIZE) {
            throw VaultCodecException.ResourceLimitExceeded(VaultResourceLimit.FILE_SIZE)
        }
        if (totalFileLength < HEADER_SIZE.toLong() + AUTHENTICATION_TAG_SIZE) {
            throw VaultCodecException.InvalidFile("File is shorter than the fixed container structure")
        }
    }

    private fun expectedFileLength(payloadLength: Long): Long {
        if (payloadLength < 0L) {
            throw VaultCodecException.InvalidFile("Payload length is negative")
        }
        val maximumPayloadLength = MAX_FILE_SIZE - HEADER_SIZE - AUTHENTICATION_TAG_SIZE
        if (payloadLength > maximumPayloadLength) {
            throw VaultCodecException.ResourceLimitExceeded(VaultResourceLimit.FILE_SIZE)
        }
        return HEADER_SIZE.toLong() + payloadLength + AUTHENTICATION_TAG_SIZE
    }

    private fun consumeExactly(input: InputStream, byteCount: Long) {
        var remaining = byteCount
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (remaining > 0L) {
            val readCount = input.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
            if (readCount == -1) {
                throw VaultCodecException.InvalidFile("File payload is truncated")
            }
            remaining -= readCount
        }
    }

    private fun consumeRemainder(input: InputStream): Boolean {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var foundTrailingData = false
        while (true) {
            val readCount = input.read(buffer)
            if (readCount == -1) return foundTrailingData
            if (readCount > 0) foundTrailingData = true
        }
    }

    private class FileSizeLimitedInputStream(
        private val source: InputStream,
    ) : InputStream() {
        private var byteCount = 0L

        override fun read(): Int {
            val value = source.read()
            if (value == -1) return -1
            increaseCount(1L)
            return value
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            val readCount = source.read(buffer, offset, length)
            if (readCount > 0) increaseCount(readCount.toLong())
            return readCount
        }

        private fun increaseCount(increment: Long) {
            byteCount += increment
            if (byteCount > MAX_FILE_SIZE) {
                throw VaultCodecException.ResourceLimitExceeded(VaultResourceLimit.FILE_SIZE)
            }
        }
    }

    private val CANONICAL_UUID_REGEX = Regex(
        "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}",
    )
}