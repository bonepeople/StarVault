package com.bonepeople.android.starvault.module.vault.create

sealed interface VaultCreateEffect {
    data class RequestCreateDocument(val vaultName: String) : VaultCreateEffect
}