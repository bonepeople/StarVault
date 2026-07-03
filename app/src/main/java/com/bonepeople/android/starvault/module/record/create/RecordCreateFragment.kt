package com.bonepeople.android.starvault.module.record.create

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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bonepeople.android.base.activity.StandardActivity
import com.bonepeople.android.base.util.FlowExtension.observeWithLifecycle
import com.bonepeople.android.starvault.global.base.BaseFragment
import com.bonepeople.android.starvault.ui.component.RecordTag
import com.bonepeople.android.starvault.module.record.detail.RecordDetailFragment

class RecordCreateFragment : BaseFragment() {
    private val viewModel: RecordCreatePageModel by viewModels()

    @Composable
    override fun Content() {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle(viewLifecycleOwner.lifecycle)
        ComposeContent(
            uiState = uiState,
            action = viewModel::dispatch,
        )
    }

    override fun initData(savedInstanceState: Bundle?) {
        viewModel.effect.observeWithLifecycle(viewLifecycleOwner, Lifecycle.State.RESUMED) {
            when (it) {
                is RecordCreateEffect.OpenDetail -> openDetail(it.recordId)
            }
        }
    }

    private fun openDetail(recordId: String) {
        StandardActivity.call(RecordDetailFragment.newInstance(recordId)).onResult {
            requireActivity().setResult(
                Activity.RESULT_OK,
                Intent().putExtra(EXTRA_RECORD_ID, recordId),
            )
            requireActivity().finishAfterTransition()
        }
    }

    @Preview(showSystemUi = true)
    @Composable
    private fun ComposeContent(
        uiState: RecordCreateState = RecordCreateState.Preview.Empty,
        action: (RecordCreateUserAction) -> Unit = {},
    ) {
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current
        val isPreview = LocalInspectionMode.current
        if (!isPreview) {
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }
        LaunchedEffect(uiState.saving) {
            if (uiState.saving) {
                keyboardController?.hide()
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { action(RecordCreateUserAction.UpdateTitle(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    label = { Text(text = "名称") },
                    isError = uiState.titleError.isNotEmpty(),
                    supportingText = if (uiState.titleError.isNotEmpty()) {
                        { Text(text = uiState.titleError) }
                    } else {
                        null
                    },
                    enabled = !uiState.saving,
                    singleLine = true,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "标签",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                RecordTag.TagSection(
                    tags = uiState.tags,
                    editable = !uiState.saving,
                    onEdit = { action(RecordCreateUserAction.ClickTags) },
                )
            }
            Button(
                onClick = { action(RecordCreateUserAction.ClickNext) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                enabled = !uiState.saving,
            ) {
                Text(text = "下一步")
            }
        }
    }

    companion object {
        const val EXTRA_RECORD_ID = "recordId"
    }
}