package com.bonepeople.android.starvault.global.storage

internal class VaultSessionKeyMaterial(
    salt: ByteArray,
    fileKey: ByteArray,
) {
    private var activeSalt: ByteArray? = null
    private var activeFileKey: ByteArray? = null

    init {
        require(salt.size == VaultFileProtocol.SALT_SIZE) { "Salt length is invalid" }
        require(fileKey.size == VaultFileProtocol.FILE_KEY_SIZE) { "File key length is invalid" }
        activeSalt = salt.copyOf()
        activeFileKey = fileKey.copyOf()
    }

    fun <T> use(action: (salt: ByteArray, fileKey: ByteArray) -> T): T {
        val (saltCopy, fileKeyCopy) = synchronized(this) {
            val salt = requireNotNull(activeSalt) { "Vault session is closed" }.copyOf()
            val key = requireNotNull(activeFileKey) { "Vault session is closed" }.copyOf()
            salt to key
        }
        return try {
            action(saltCopy, fileKeyCopy)
        } finally {
            saltCopy.fill(0)
            fileKeyCopy.fill(0)
        }
    }

    @Synchronized
    fun close() {
        activeSalt?.fill(0)
        activeFileKey?.fill(0)
        activeSalt = null
        activeFileKey = null
    }
}