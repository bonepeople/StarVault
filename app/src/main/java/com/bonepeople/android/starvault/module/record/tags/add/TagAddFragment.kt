package com.bonepeople.android.starvault.module.record.tags.add

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

class TagAddFragment : BaseFragment() {
    private val viewModel: TagAddPageModel by viewModels()

    @Composable
    override fun Content() {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle(viewLifecycleOwner.lifecycle)
        ComposeContent(uiState = uiState, action = viewModel::dispatch)
    }

    override fun initData(savedInstanceState: Bundle?) {
        viewModel.effect.observeWithLifecycle(viewLifecycleOwner, Lifecycle.State.RESUMED) { effect ->
            when (effect) {
                is TagAddEffect.FinishAdded -> finishAdded(effect.tag)
            }
        }
        val existingTags = requireArguments().getStringArrayList(ARG_EXISTING_TAGS).orEmpty()
        viewModel.init(existingTags)
    }

    private fun finishAdded(tag: String) {
        val intent = Intent().putExtra(EXTRA_TAG, tag)
        requireActivity().setResult(Activity.RESULT_OK, intent)
        requireActivity().finishAfterTransition()
    }

    @Preview(showSystemUi = true)
    @Composable
    private fun ComposeContent(
        uiState: TagAddState = TagAddState.Preview.Normal,
        action: (TagAddUserAction) -> Unit = {},
    ) {
        val nameFocusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val isPreview = LocalInspectionMode.current
        var nameFieldValue by remember {
            mutableStateOf(TextFieldValue(text = uiState.name, selection = TextRange(uiState.name.length)))
        }
        LaunchedEffect(uiState.name) {
            if (nameFieldValue.text != uiState.name) {
                nameFieldValue = TextFieldValue(text = uiState.name, selection = TextRange(uiState.name.length))
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
                text = "添加标签",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = nameFieldValue,
                onValueChange = {
                    nameFieldValue = it
                    action(TagAddUserAction.UpdateName(it.text))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(nameFocusRequester),
                label = { Text(text = "标签") },
                isError = uiState.nameError.isNotEmpty(),
                supportingText = if (uiState.nameError.isNotEmpty()) {
                    { Text(text = uiState.nameError) }
                } else {
                    null
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        action(TagAddUserAction.ClickConfirm)
                    },
                ),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = ClickExtension.rememberSingleClick { action(TagAddUserAction.ClickConfirm) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.submitting,
            ) {
                Text(text = "添加")
            }
        }
    }

    companion object {
        private const val EXTRA_TAG = "tag"
        private const val ARG_EXISTING_TAGS = "existing_tags"

        fun parseResult(intent: Intent?): String? {
            return intent?.getStringExtra(EXTRA_TAG)?.takeIf { it.isNotEmpty() }
        }

        fun newInstance(existingTags: List<String>): TagAddFragment {
            return TagAddFragment().apply {
                arguments = bundleOf(ARG_EXISTING_TAGS to ArrayList(existingTags))
            }
        }
    }
}