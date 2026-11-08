package com.bonepeople.android.starvault.module.vault.create

sealed interface VaultCreateUserAction {
    data class UpdateName(val name: String) : VaultCreateUserAction

    data class UpdatePassword(val password: String) : VaultCreateUserAction

    data class UpdateConfirmPassword(val password: String) : VaultCreateUserAction

    object ClickContinue : VaultCreateUserAction
}