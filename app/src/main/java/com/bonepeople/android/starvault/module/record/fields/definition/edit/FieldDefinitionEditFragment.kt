package com.bonepeople.android.starvault.module.record.fields.definition.edit

import android.app.Activity
import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bonepeople.android.base.util.FlowExtension.observeWithLifecycle
import com.bonepeople.android.starvault.global.base.BaseFragment
import com.bonepeople.android.starvault.global.util.ClickExtension

class FieldDefinitionEditFragment : BaseFragment() {
    private val viewModel: FieldDefinitionEditPageModel by viewModels()

    @Composable
    override fun Content() {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle(viewLifecycleOwner.lifecycle)
        ComposeContent(uiState = uiState, action = viewModel::dispatch)
    }

    override fun initData(savedInstanceState: Bundle?) {
        viewModel.effect.observeWithLifecycle(viewLifecycleOwner, Lifecycle.State.RESUMED) { effect ->
            when (effect) {
                is FieldDefinitionEditEffect.FinishCreated -> finishCreated(effect.definitionId)
            }
        }
        val definitionId = requireArguments().getString(ARG_DEFINITION_ID).orEmpty()
        viewModel.init(definitionId)
    }

    private fun finishCreated(definitionId: String) {
        val intent = Intent().putExtra(EXTRA_DEFINITION_ID, definitionId)
        requireActivity().setResult(Activity.RESULT_OK, intent)
        requireActivity().finishAfterTransition()
    }

    @Preview(showSystemUi = true)
    @Composable
    private fun ComposeContent(
        uiState: FieldDefinitionEditState = FieldDefinitionEditState.Preview.Normal,
        action: (FieldDefinitionEditUserAction) -> Unit = {},
    ) {
        val nameFocusRequester = remember { FocusRequester() }
        val descriptionFocusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val isPreview = LocalInspectionMode.current
        var nameFieldValue by remember {
            mutableStateOf(TextFieldValue(text = uiState.name, selection = TextRange(uiState.name.length)))
        }
        var descriptionFieldValue by remember {
            mutableStateOf(TextFieldValue(text = uiState.description, selection = TextRange(uiState.description.length)))
        }
        LaunchedEffect(uiState.name) {
            if (nameFieldValue.text != uiState.name) {
                nameFieldValue = TextFieldValue(text = uiState.name, selection = TextRange(uiState.name.length))
            }
        }
        LaunchedEffect(uiState.description) {
            if (descriptionFieldValue.text != uiState.description) {
                descriptionFieldValue = TextFieldValue(
                    text = uiState.description,
                    selection = TextRange(uiState.description.length),
                )
            }
        }
        if (!isPreview) {
            LaunchedEffect(Unit) {
                nameFocusRequester.requestFocus()
                keyboardController?.show()
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Text(
                text = "新建信息类型",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = nameFieldValue,
                onValueChange = {
                    nameFieldValue = it
                    action(FieldDefinitionEditUserAction.UpdateName(it.text))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(nameFocusRequester),
                label = { Text(text = "名称") },
                isError = uiState.nameError.isNotEmpty(),
                supportingText = if (uiState.nameError.isNotEmpty()) {
                    { Text(text = uiState.nameError) }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { descriptionFocusRequester.requestFocus() }),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = descriptionFieldValue,
                onValueChange = {
                    descriptionFieldValue = it
                    action(FieldDefinitionEditUserAction.UpdateDescription(it.text))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(descriptionFocusRequester),
                label = { Text(text = "说明（可选）") },
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
                onClick = ClickExtension.rememberSingleClick { action(FieldDefinitionEditUserAction.ClickConfirm) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.saving,
            ) {
                Text(text = if (uiState.saving) "创建中…" else "确认创建")
            }
        }
    }

    companion object {
        private const val EXTRA_DEFINITION_ID = "definition_id"
        private const val ARG_DEFINITION_ID = "definition_id"

        fun parseResult(intent: Intent?): String? {
            return intent?.getStringExtra(EXTRA_DEFINITION_ID)?.takeIf { it.isNotEmpty() }
        }

        fun newInstance(definitionId: String): FieldDefinitionEditFragment {
            return FieldDefinitionEditFragment().apply {
                arguments = bundleOf(ARG_DEFINITION_ID to definitionId)
            }
        }
    }
}