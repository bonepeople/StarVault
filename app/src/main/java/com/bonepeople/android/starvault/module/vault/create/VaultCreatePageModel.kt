package com.bonepeople.android.starvault.module.vault.create

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class VaultCreatePageModel : ViewModel() {
    val uiState: MutableStateFlow<VaultCreateState> = MutableStateFlow(VaultCreateState.Default)
    private val _effect = Channel<VaultCreateEffect>(capacity = Channel.UNLIMITED)
    val effect = _effect.receiveAsFlow()

    fun dispatch(action: VaultCreateUserAction) {
        when (action) {
            is VaultCreateUserAction.UpdateName -> updateName(action.name)
            is VaultCreateUserAction.UpdatePassword -> updatePassword(action.password)
            is VaultCreateUserAction.UpdateConfirmPassword -> updateConfirmPassword(action.password)
            VaultCreateUserAction.ClickContinue -> requestCreateDocument()
        }
    }

    private fun updateName(name: String) {
        if (uiState.value.requestingFile) return
        uiState.update { it.copy(name = name, nameError = "") }
    }

    private fun updatePassword(password: String) {
        if (uiState.value.requestingFile) return
        uiState.update {
            it.copy(
                password = password,
                passwordError = "",
                confirmPasswordError = "",
            )
        }
    }

    private fun updateConfirmPassword(password: String) {
        if (uiState.value.requestingFile) return
        uiState.update { it.copy(confirmPassword = password, confirmPasswordError = "") }
    }

    private fun requestCreateDocument() {
        val currentState = uiState.value
        if (currentState.requestingFile) return

        val trimmedName = currentState.name.trim()
        val nameError = if (trimmedName.isEmpty()) "请输入密码库名称" else ""
        val passwordError = if (currentState.password.isEmpty()) "请输入主密码" else ""
        val confirmPasswordError = when {
            currentState.password.isEmpty() -> ""
            currentState.password != currentState.confirmPassword -> "两次输入的主密码不一致"
            else -> ""
        }
        if (nameError.isNotEmpty() || passwordError.isNotEmpty() || confirmPasswordError.isNotEmpty()) {
            uiState.update {
                it.copy(
                    nameError = nameError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError,
                )
            }
            return
        }

        uiState.update {
            it.copy(
                name = trimmedName,
                nameError = "",
                passwordError = "",
                confirmPasswordError = "",
                requestingFile = true,
            )
        }
        _effect.trySend(VaultCreateEffect.RequestCreateDocument(trimmedName))
    }
}