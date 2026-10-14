package com.bonepeople.android.starvault.module.record.fields.edit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bonepeople.android.base.util.FlowExtension.observeWithLifecycle
import com.bonepeople.android.starvault.global.base.BaseFragment
import com.bonepeople.android.starvault.global.data.VaultFieldInfo
import com.bonepeople.android.widget.util.AppGson
import com.bonepeople.android.widget.util.AppTime

class FieldsEditFragment : BaseFragment() {
    private val viewModel: FieldsEditPageModel by viewModels()
    private var focusTarget: FieldsEditEffect.FocusTarget? by mutableStateOf(null)

    @Composable
    override fun Content() {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle(viewLifecycleOwner.lifecycle)
        ComposeContent(uiState = uiState, focusTarget = focusTarget, action = viewModel::dispatch)
    }

    override fun initData(savedInstanceState: Bundle?) {
        viewModel.effect.observeWithLifecycle(viewLifecycleOwner, Lifecycle.State.RESUMED) { effect ->
            when (effect) {
                is FieldsEditEffect.RequestFocus -> focusTarget = effect.target
                is FieldsEditEffect.FinishSaved -> finishSaved(effect.field)
            }
        }

        val field = runCatching {
            val fieldJson = requireArguments().getString(ARG_FIELD_JSON)
            AppGson.toObject<VaultFieldInfo>(fieldJson)
        }.getOrDefault(VaultFieldInfo())
        viewModel.init(field)
    }

    private fun finishSaved(field: VaultFieldInfo) {
        val result = Intent().putExtra(EXTRA_FIELD_JSON, AppGson.toJson(field))
        requireActivity().setResult(Activity.RESULT_OK, result)
        requireActivity().finishAfterTransition()
    }

    @Preview(showSystemUi = true)
    @Composable
    private fun ComposeContent(
        uiState: FieldsEditState = FieldsEditState.Preview.Normal,
        focusTarget: FieldsEditEffect.FocusTarget? = null,
        action: (FieldsEditUserAction) -> Unit = {},
    ) {
        val nameFocusRequester = remember { FocusRequester() }
        val valueFocusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        LaunchedEffect(focusTarget) {
            when (focusTarget) {
                FieldsEditEffect.FocusTarget.Name -> nameFocusRequester.requestFocus()
                FieldsEditEffect.FocusTarget.Value -> valueFocusRequester.requestFocus()
                null -> return@LaunchedEffect
            }
            keyboardController?.show()
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Text(
                text = if (uiState.id.isEmpty()) "添加字段" else "编辑字段",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { action(FieldsEditUserAction.UpdateName(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(nameFocusRequester),
                label = { Text(text = "字段名称") },
                isError = uiState.nameError.isNotEmpty(),
                supportingText = if (uiState.nameError.isNotEmpty()) {
                    { Text(text = uiState.nameError) }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { valueFocusRequester.requestFocus() }),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.value,
                onValueChange = { action(FieldsEditUserAction.UpdateValue(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(valueFocusRequester),
                label = { Text(text = "字段值") },
                isError = uiState.valueError.isNotEmpty(),
                supportingText = if (uiState.valueError.isNotEmpty()) {
                    { Text(text = uiState.valueError) }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                ),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { action(FieldsEditUserAction.ClickConfirm) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "确认")
            }
            if (uiState.history.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "历史记录",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    uiState.history.forEach { item ->
                        HistoryItem(item = item)
                    }
                }
            }
        }
    }

    @Composable
    private fun HistoryItem(item: FieldsEditState.HistoryItem) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.value.ifEmpty { "—" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = AppTime.formatTime(item.createTimestamp, "yyyy-MM-dd HH:mm"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        }
    }

    companion object {
        const val EXTRA_FIELD_JSON = "field_json"
        private const val ARG_FIELD_JSON = "field_json"

        fun newInstance(field: VaultFieldInfo): FieldsEditFragment {
            return FieldsEditFragment().apply {
                arguments = bundleOf(ARG_FIELD_JSON to AppGson.toJson(field))
            }
        }
    }
}