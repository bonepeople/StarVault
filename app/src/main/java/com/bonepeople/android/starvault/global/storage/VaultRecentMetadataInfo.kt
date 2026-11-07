package com.bonepeople.android.starvault.global.storage

import android.net.Uri
import java.util.UUID

data class VaultRecentMetadataInfo(
    val uri: Uri,
    val vaultId: String,
    val displayName: String,
    val revision: Long,
) {
    init {
        require(uri.toString().isNotEmpty()) { "URI is empty" }
        require(isCanonicalUuid(vaultId)) { "Vault ID is invalid" }
        require(revision >= VaultFileProtocol.INITIAL_REVISION) { "Revision is invalid" }
    }

    private fun isCanonicalUuid(value: String): Boolean {
        return try {
            UUID.fromString(value).toString() == value
        } catch (_: IllegalArgumentException) {
            false
        }
    }
}