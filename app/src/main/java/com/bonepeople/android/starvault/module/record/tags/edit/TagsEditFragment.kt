package com.bonepeople.android.starvault.module.record.tags.edit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bonepeople.android.base.util.FlowExtension.observeWithLifecycle
import com.bonepeople.android.starvault.global.base.BaseFragment
import kotlinx.coroutines.launch

class TagsEditFragment : BaseFragment() {
    private val viewModel: TagsEditPageModel by viewModels()

    @Composable
    override fun Content() {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle(viewLifecycleOwner.lifecycle)
        ComposeContent(uiState = uiState, action = viewModel::dispatch)
    }

    override fun initData(savedInstanceState: Bundle?) {
        viewModel.effect.observeWithLifecycle(viewLifecycleOwner, Lifecycle.State.RESUMED) { effect ->
            when (effect) {
                is TagsEditEffect.FinishWithResult -> finishWithResult(effect.tags)
            }
        }
        val initialTags = requireArguments().getStringArrayList(ARG_INITIAL_TAGS).orEmpty()
        viewModel.init(initialTags)
    }

    override fun handleBackPress() {
        viewModel.dispatch(TagsEditUserAction.ClickBack)
    }

    private fun finishWithResult(tags: List<String>) {
        requireActivity().setResult(Activity.RESULT_OK, Intent().putStringArrayListExtra(EXTRA_TAGS, ArrayList(tags)))
        requireActivity().finishAfterTransition()
    }

    @Preview(showSystemUi = true)
    @Composable
    private fun ComposeContent(uiState: TagsEditState = TagsEditState.Preview.Normal, action: (TagsEditUserAction) -> Unit = {}) {
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(uiState.pendingUndo) {
            val undo = uiState.pendingUndo ?: return@LaunchedEffect
            launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showSnackbar(
                    message = "已删除「${undo.tag}」",
                    actionLabel = "撤回",
                )
                if (result == SnackbarResult.ActionPerformed) {
                    action(TagsEditUserAction.UndoDelete)
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Text(
                    text = "编辑标签",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(16.dp))
                TagList(
                    tags = uiState.tags,
                    onDelete = { action(TagsEditUserAction.ClickDelete(it)) },
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .clickable { action(TagsEditUserAction.ClickOpenAddDialog) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "添加标签",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.weight(1f))
                if (uiState.recommendedTags.isNotEmpty()) {
                    RecommendedTagSection(
                        tags = uiState.recommendedTags,
                        onSelect = { action(TagsEditUserAction.ClickRecommendTag(it)) },
                    )
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
        if (uiState.addDialogVisible) {
            AddTagDialog(
                inputText = uiState.inputText,
                inputError = uiState.inputError,
                onInputChange = { action(TagsEditUserAction.UpdateInput(it)) },
                onConfirm = { action(TagsEditUserAction.ClickConfirmAdd) },
                onDismiss = { action(TagsEditUserAction.DismissAddDialog) },
            )
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun TagList(
        tags: List<String>,
        onDelete: (String) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        if (tags.isEmpty()) {
            Text(
                text = "暂无标签",
                modifier = modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            )
        } else {
            FlowRow(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                tags.forEach { tag ->
                    DeletableTagChip(
                        text = tag,
                        onDelete = { onDelete(tag) },
                    )
                }
            }
        }
    }

    @Composable
    private fun DeletableTagChip(
        text: String,
        onDelete: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Row(
            modifier = modifier
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(6.dp),
                )
                .padding(start = 8.dp, end = 2.dp, top = 3.dp, bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "删除",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.65f),
                )
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun RecommendedTagSection(
        tags: List<String>,
        onSelect: (String) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "推荐标签",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                tags.forEach { tag ->
                    SelectableTagChip(
                        text = tag,
                        onClick = { onSelect(tag) },
                    )
                }
            }
        }
    }

    @Composable
    private fun SelectableTagChip(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Text(
            text = text,
            modifier = modifier
                .clickable(onClick = onClick)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(6.dp),
                )
                .padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }

    @Composable
    private fun AddTagDialog(
        inputText: String,
        inputError: String,
        onInputChange: (String) -> Unit,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
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
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "添加标签") },
            text = {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    label = { Text(text = "新标签") },
                    isError = inputError.isNotEmpty(),
                    supportingText = if (inputError.isNotEmpty()) {
                        { Text(text = inputError) }
                    } else {
                        null
                    },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(text = "添加")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = "取消")
                }
            },
        )
    }

    companion object {
        const val EXTRA_TAGS = "tags"
        private const val ARG_INITIAL_TAGS = "initial_tags"

        fun newInstance(initialTags: List<String>): TagsEditFragment {
            return TagsEditFragment().apply {
                arguments = bundleOf(ARG_INITIAL_TAGS to ArrayList(initialTags))
            }
        }
    }
}