package com.bonepeople.android.starvault.global.storage

data class VaultFileHeaderInfo(
    val containerVersion: Int = VaultFileProtocol.CONTAINER_VERSION,
    val vaultId: String,
    val kdfId: Int = VaultFileProtocol.KDF_ID_ARGON2ID,
    val argon2Version: Int = VaultFileProtocol.ARGON2_VERSION,
    val argon2MemoryKib: Int = VaultFileProtocol.ARGON2_MEMORY_KIB,
    val argon2Iterations: Int = VaultFileProtocol.ARGON2_ITERATIONS,
    val argon2Parallelism: Int = VaultFileProtocol.ARGON2_PARALLELISM,
    val salt: ByteArray,
    val cipherId: Int = VaultFileProtocol.CIPHER_ID_AES_256_GCM,
    val nonce: ByteArray,
    val payloadLength: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VaultFileHeaderInfo) return false

        return containerVersion == other.containerVersion &&
            vaultId == other.vaultId &&
            kdfId == other.kdfId &&
            argon2Version == other.argon2Version &&
            argon2MemoryKib == other.argon2MemoryKib &&
            argon2Iterations == other.argon2Iterations &&
            argon2Parallelism == other.argon2Parallelism &&
            salt.contentEquals(other.salt) &&
            cipherId == other.cipherId &&
            nonce.contentEquals(other.nonce) &&
            payloadLength == other.payloadLength
    }

    override fun hashCode(): Int {
        var result = containerVersion
        result = 31 * result + vaultId.hashCode()
        result = 31 * result + kdfId
        result = 31 * result + argon2Version
        result = 31 * result + argon2MemoryKib
        result = 31 * result + argon2Iterations
        result = 31 * result + argon2Parallelism
        result = 31 * result + salt.contentHashCode()
        result = 31 * result + cipherId
        result = 31 * result + nonce.contentHashCode()
        result = 31 * result + payloadLength.hashCode()
        return result
    }
}