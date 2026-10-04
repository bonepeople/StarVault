package com.bonepeople.android.starvault.module.record.fields.edit

import android.os.Bundle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

class FieldsEditFragment : BaseFragment() {
    private val viewModel: FieldsEditPageModel by viewModels()

    @Composable
    override fun Content() {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle(viewLifecycleOwner.lifecycle)
        ComposeContent(uiState = uiState, action = viewModel::dispatch)
    }

    override fun initData(savedInstanceState: Bundle?) {
        viewModel.effect.observeWithLifecycle(viewLifecycleOwner, Lifecycle.State.RESUMED) { effect ->
            when (effect) {
                FieldsEditEffect.Finish -> requireActivity().finishAfterTransition()
            }
        }

        val field = runCatching {
            val fieldJson = requireArguments().getString(ARG_FIELD_JSON)
            AppGson.toObject<VaultFieldInfo>(fieldJson)
        }.getOrDefault(VaultFieldInfo())
        viewModel.init(field)
    }

    override fun handleBackPress() {
        viewModel.dispatch(FieldsEditUserAction.ClickBack)
    }

    @Preview(showSystemUi = true)
    @Composable
    private fun ComposeContent(uiState: FieldsEditState = FieldsEditState.Preview.Normal, action: (FieldsEditUserAction) -> Unit = {}) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Text(
                text = "编辑字段",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { action(FieldsEditUserAction.UpdateName(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "字段名称") },
                isError = uiState.nameError.isNotEmpty(),
                supportingText = if (uiState.nameError.isNotEmpty()) {
                    { Text(text = uiState.nameError) }
                } else {
                    null
                },
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.value,
                onValueChange = { action(FieldsEditUserAction.UpdateValue(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "字段值") },
                isError = uiState.valueError.isNotEmpty(),
                supportingText = if (uiState.valueError.isNotEmpty()) {
                    { Text(text = uiState.valueError) }
                } else {
                    null
                },
                singleLine = true,
            )
        }
    }

    companion object {
        private const val ARG_FIELD_JSON = "field_json"

        fun newInstance(field: VaultFieldInfo): FieldsEditFragment {
            return FieldsEditFragment().apply {
                arguments = bundleOf(ARG_FIELD_JSON to AppGson.toJson(field))
            }
        }
    }
}