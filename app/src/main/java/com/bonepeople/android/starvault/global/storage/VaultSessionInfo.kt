package com.bonepeople.android.starvault.global.storage

import android.net.Uri
import com.bonepeople.android.starvault.global.data.VaultInfo
import java.util.UUID

class VaultSessionInfo internal constructor(
    val uri: Uri,
    payload: VaultPayloadInfo,
    private val keyMaterial: VaultSessionKeyMaterial,
) {
    private var activePayload: VaultPayloadInfo? = payload

    init {
        try {
            require(uri.toString().isNotEmpty()) { "URI is empty" }
            require(payload.schemaVersion == VaultFileProtocol.SCHEMA_VERSION) { "Schema version is invalid" }
            require(payload.revision >= VaultFileProtocol.INITIAL_REVISION) { "Revision is invalid" }
            require(isCanonicalUuid(payload.vault.id)) { "Vault ID is invalid" }
        } catch (throwable: Throwable) {
            keyMaterial.close()
            activePayload = null
            throw throwable
        }
    }

    val vaultId: String
        get() = synchronized(this) { requirePayload().vault.id }

    val revision: Long
        get() = synchronized(this) { requirePayload().revision }

    internal val vault: VaultInfo
        get() = synchronized(this) { requirePayload().vault }

    internal fun <T> useKeyMaterial(action: (salt: ByteArray, fileKey: ByteArray) -> T): T {
        synchronized(this) { requirePayload() }
        return keyMaterial.use(action)
    }

    @Synchronized
    internal fun updateVault(vault: VaultInfo) {
        require(vault.id == requirePayload().vault.id) { "Vault ID cannot change during a session" }
        activePayload = requirePayload().copy(vault = vault)
    }

    @Synchronized
    internal fun snapshotPayload(): VaultPayloadInfo {
        return requirePayload()
    }

    @Synchronized
    internal fun close() {
        keyMaterial.close()
        activePayload = null
    }

    private fun requirePayload(): VaultPayloadInfo {
        return requireNotNull(activePayload) { "Vault session is closed" }
    }

    private fun isCanonicalUuid(value: String): Boolean {
        return try {
            UUID.fromString(value).toString() == value
        } catch (_: IllegalArgumentException) {
            false
        }
    }
}