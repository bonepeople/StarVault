package com.bonepeople.android.starvault.module.vault.create

data class VaultCreateState(
    val name: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String = "",
    val passwordError: String = "",
    val confirmPasswordError: String = "",
    val requestingFile: Boolean = false,
) {
    @Suppress("unused")
    object Preview {
        val Normal = VaultCreateState()
        val Filled = VaultCreateState(
            name = "个人密码库",
            password = "preview-password",
            confirmPassword = "preview-password",
        )
        val WithErrors = VaultCreateState(
            nameError = "请输入密码库名称",
            passwordError = "请输入主密码",
            confirmPasswordError = "两次输入的主密码不一致",
        )
        val RequestingFile = Filled.copy(requestingFile = true)
    }

    companion object {
        val Default = VaultCreateState()
    }
}