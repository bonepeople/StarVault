package com.bonepeople.android.starvault.module.record.detail

import android.app.Activity
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bonepeople.android.base.activity.StandardActivity
import com.bonepeople.android.base.util.FlowExtension.observeWithLifecycle
import com.bonepeople.android.starvault.global.base.BaseFragment
import com.bonepeople.android.starvault.global.util.ClickExtension.singleClick
import com.bonepeople.android.starvault.module.record.fields.edit.FieldsEditFragment
import com.bonepeople.android.starvault.module.record.tags.edit.TagsEditFragment
import com.bonepeople.android.starvault.ui.component.RecordTag

class RecordDetailFragment : BaseFragment() {
    private val viewModel: RecordDetailPageModel by viewModels()

    @Composable
    override fun Content() {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle(viewLifecycleOwner.lifecycle)
        ComposeContent(uiState = uiState, action = viewModel::dispatch)
    }

    override fun initData(savedInstanceState: Bundle?) {
        viewModel.effect.observeWithLifecycle(viewLifecycleOwner, Lifecycle.State.RESUMED) { effect ->
            when (effect) {
                is RecordDetailEffect.OpenTagsEdit -> openTagsEdit(effect.tags)
                is RecordDetailEffect.OpenFieldsEdit -> StandardActivity.open(FieldsEditFragment.newInstance(effect.field))
            }
        }
        val recordId = requireArguments().getString(ARG_RECORD_ID).orEmpty()
        viewModel.init(recordId)
    }

    private fun openTagsEdit(tags: List<String>) {
        StandardActivity.call(TagsEditFragment.newInstance(tags)).onSuccess { intent ->
            val resultTags = intent?.getStringArrayListExtra(TagsEditFragment.EXTRA_TAGS) ?: return@onSuccess
            if (resultTags == tags) return@onSuccess
            viewModel.dispatch(RecordDetailUserAction.TagsUpdated(resultTags))
            requireActivity().setResult(Activity.RESULT_OK)
        }
    }

    @Preview(showSystemUi = true)
    @Composable
    private fun ComposeContent(uiState: RecordDetailState = RecordDetailState.Preview.Normal, action: (RecordDetailUserAction) -> Unit = {}) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            when {
                uiState.loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.notFound -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "记录不存在",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        item(key = "HEADER") {
                            Header(
                                title = uiState.title,
                                tags = uiState.tags,
                                onEditTags = { action(RecordDetailUserAction.ClickTags) },
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            )
                        }
                        items(
                            items = uiState.fields,
                            key = { it.id },
                            contentType = { "CONTENT_TYPE_FIELD_ITEM" },
                        ) { field ->
                            FieldRow(
                                field = field,
                                onClick = {
                                    action(RecordDetailUserAction.ClickField(field.id))
                                },
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            )
                        }
                        item(key = "ADD_FIELD", contentType = "CONTENT_TYPE_ADD_FIELD_ITEM") {
                            AddFieldRow(onClick = { action(RecordDetailUserAction.ClickAddField) })
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun Header(
        title: String,
        tags: List<String>,
        onEditTags: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title.ifEmpty { "未命名" },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            RecordTag.TagSection(tags = tags, editable = true, onEdit = onEditTags)
        }
    }

    @Composable
    private fun AddFieldRow(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .singleClick(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "添加字段",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    @Composable
    private fun FieldRow(
        field: RecordDetailState.Field,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .singleClick(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = field.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = field.currentValue.ifEmpty { "—" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    companion object {
        private const val ARG_RECORD_ID = "record_id"

        fun newInstance(recordId: String): RecordDetailFragment {
            return RecordDetailFragment().apply {
                arguments = bundleOf(ARG_RECORD_ID to recordId)
            }
        }
    }
}