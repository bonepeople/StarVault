package com.bonepeople.android.starvault.module.vault.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bonepeople.android.starvault.global.base.BaseFragment
import com.bonepeople.android.starvault.global.util.ClickExtension

class VaultCreateFragment : BaseFragment() {
    private val viewModel: VaultCreatePageModel by viewModels()

    @Composable
    override fun Content() {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle(viewLifecycleOwner.lifecycle)
        ComposeContent(uiState = uiState, action = viewModel::dispatch)
    }

    @Preview(showSystemUi = true)
    @Composable
    private fun ComposeContent(
        uiState: VaultCreateState = VaultCreateState.Preview.Normal,
        action: (VaultCreateUserAction) -> Unit = {},
    ) {
        val passwordFocusRequester = remember { FocusRequester() }
        val confirmPasswordFocusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "创建密码库",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = "StarVault 会在你选择的位置创建一个独立的加密 .starvault 文件。你可以在接下来的系统界面中修改文件名和保存位置。",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { action(VaultCreateUserAction.UpdateName(it)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.requestingFile,
                label = { Text(text = "密码库名称") },
                isError = uiState.nameError.isNotEmpty(),
                supportingText = errorSupportingText(uiState.nameError),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() }),
            )
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { action(VaultCreateUserAction.UpdatePassword(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                enabled = !uiState.requestingFile,
                label = { Text(text = "主密码") },
                isError = uiState.passwordError.isNotEmpty(),
                supportingText = errorSupportingText(uiState.passwordError),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(onNext = { confirmPasswordFocusRequester.requestFocus() }),
            )
            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = { action(VaultCreateUserAction.UpdateConfirmPassword(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(confirmPasswordFocusRequester),
                enabled = !uiState.requestingFile,
                label = { Text(text = "确认主密码") },
                isError = uiState.confirmPasswordError.isNotEmpty(),
                supportingText = errorSupportingText(uiState.confirmPasswordError),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            )
            Button(
                onClick = ClickExtension.rememberSingleClick {
                    focusManager.clearFocus()
                    action(VaultCreateUserAction.ClickContinue)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.requestingFile,
            ) {
                Text(text = if (uiState.requestingFile) "正在请求文件位置…" else "继续")
            }
        }
    }

    @Composable
    private fun errorSupportingText(error: String): (@Composable () -> Unit)? {
        return if (error.isNotEmpty()) {
            { Text(text = error) }
        } else {
            null
        }
    }
}