package com.bonepeople.android.starvault.module.record.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bonepeople.android.widget.util.AppTime

class RecordDetailFragment : Fragment() {
    private val viewModel: RecordDetailPageModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recordId = requireArguments().getString(ARG_RECORD_ID).orEmpty()
        (view as ComposeView).setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle(viewLifecycleOwner.lifecycle)
            ComposeContent(uiState = uiState, action = viewModel::dispatch)
        }
        viewModel.init(recordId)
    }

    @Preview(showSystemUi = true)
    @Composable
    private fun ComposeContent(
        uiState: RecordDetailState = RecordDetailState(
            title = "GitHub",
            tags = listOf("开发", "工作"),
            fields = listOf(
                RecordDetailState.Field(
                    id = "1",
                    name = "用户名",
                    currentValue = "bonepeople",
                ),
                RecordDetailState.Field(
                    id = "2",
                    name = "密码",
                    currentValue = "P@ssw0rd_2026!",
                    history = listOf(
                        RecordDetailState.Field.HistoryItem("TmpP@ss2025", 1_758_355_200_000L),
                        RecordDetailState.Field.HistoryItem("OldP@ss2024", 1_717_228_800_000L),
                    ),
                    historyExpanded = true,
                ),
                RecordDetailState.Field(
                    id = "3",
                    name = "备注",
                    currentValue = "主账号，绑定 YubiKey",
                ),
            ),
        ),
        action: (RecordDetailUserAction) -> Unit = {},
    ) {
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
                            RecordDetailHeader(
                                title = uiState.title,
                                tags = uiState.tags,
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
                            RecordDetailFieldRow(
                                field = field,
                                onToggleHistory = {
                                    action(RecordDetailUserAction.ToggleFieldHistory(field.id))
                                },
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }
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

@Composable
private fun RecordDetailHeader(
    title: String,
    tags: List<String>,
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
        RecordDetailTagsRow(tags = tags)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecordDetailTagsRow(tags: List<String>) {
    if (tags.isEmpty()) {
        Text(
            text = "无标签",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
        )
        return
    }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tags.forEach { tag ->
            RecordDetailTagChip(text = tag)
        }
    }
}

@Composable
private fun RecordDetailTagChip(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                shape = RoundedCornerShape(6.dp),
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun RecordDetailFieldRow(
    field: RecordDetailState.Field,
    onToggleHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
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
        if (field.history.isNotEmpty()) {
            Text(
                text = if (field.historyExpanded) {
                    "收起历史（${field.history.size}）"
                } else {
                    "查看历史（${field.history.size}）"
                },
                modifier = Modifier
                    .clickable(onClick = onToggleHistory)
                    .padding(vertical = 2.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (field.historyExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    field.history.forEach { item ->
                        RecordDetailHistoryItem(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordDetailHistoryItem(item: RecordDetailState.Field.HistoryItem) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = item.value.ifEmpty { "—" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )
        Text(
            text = AppTime.formatTime(item.createTimestamp, "yyyy-MM-dd"),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
    }
}