package com.bonepeople.android.starvault.module.record.list

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class RecordListFragment : Fragment() {
    private val viewModel: RecordListPageModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (view as ComposeView).setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle(viewLifecycleOwner.lifecycle)
            ComposeContent(uiState = uiState, action = viewModel::dispatch)
        }
        viewModel.init()
    }

    @Preview(showSystemUi = true)
    @Composable
    private fun ComposeContent(
        uiState: RecordListState = RecordListState(
            items = listOf(
                RecordListState.Item(id = "1", title = "GitHub", tags = listOf("开发", "工作")),
                RecordListState.Item(id = "2", title = "Wi‑Fi 密码", tags = listOf("Home")),
                RecordListState.Item(id = "3", title = "无标签示例", tags = emptyList()),
            ),
        ),
        action: (RecordListUserAction) -> Unit = {},
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

                uiState.items.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "暂无记录",
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
                        items(
                            items = uiState.items,
                            key = { it.id },
                            contentType = { "CONTENT_TYPE_RECORD_ITEM" },
                        ) { item ->
                            RecordListItemRow(
                                item = item,
                                onClick = { action(RecordListUserAction.ClickItem(item.id)) },
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
}

@Composable
private fun RecordListItemRow(
    item: RecordListState.Item,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = item.title.ifEmpty { "未命名" },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        RecordTagsRow(tags = item.tags)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecordTagsRow(tags: List<String>) {
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
            RecordTagChip(text = tag)
        }
    }
}

@Composable
private fun RecordTagChip(text: String) {
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