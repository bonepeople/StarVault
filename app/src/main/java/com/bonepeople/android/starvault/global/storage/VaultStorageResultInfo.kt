package com.bonepeople.android.starvault.global.storage

data class VaultStorageResultInfo internal constructor(
    val session: VaultSessionInfo,
    val displayName: String,
    val vaultId: String,
    val revision: Long,
)