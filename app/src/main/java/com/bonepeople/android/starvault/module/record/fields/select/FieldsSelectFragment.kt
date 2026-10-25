package com.bonepeople.android.starvault.module.record.fields.select

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.bonepeople.android.base.util.FlowExtension.observeWithLifecycle
import com.bonepeople.android.starvault.global.base.BaseFragment
import com.bonepeople.android.starvault.global.util.ClickExtension.singleClick

class FieldsSelectFragment : BaseFragment() {
    private val viewModel: FieldsSelectPageModel by viewModels()

    @Composable
    override fun Content() {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle(viewLifecycleOwner.lifecycle)
        ComposeContent(uiState = uiState, action = viewModel::dispatch)
    }

    override fun initData(savedInstanceState: Bundle?) {
        viewModel.effect.observeWithLifecycle(viewLifecycleOwner, Lifecycle.State.RESUMED) { effect ->
            when (effect) {
                is FieldsSelectEffect.FinishSelected -> finishSelected(effect.definitionId)
            }
        }
        val usedDefinitionIds = requireArguments().getStringArrayList(ARG_USED_DEFINITION_IDS).orEmpty()
        viewModel.init(usedDefinitionIds)
    }

    private fun finishSelected(definitionId: String) {
        val intent = Intent().putExtra(EXTRA_DEFINITION_ID, definitionId)
        requireActivity().setResult(Activity.RESULT_OK, intent)
        requireActivity().finishAfterTransition()
    }

    @Preview(showSystemUi = true)
    @Composable
    private fun ComposeContent(
        uiState: FieldsSelectState = FieldsSelectState.Preview.Normal,
        action: (FieldsSelectUserAction) -> Unit = {},
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Text(
                text = "选择要添加的信息类型",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { action(FieldsSelectUserAction.UpdateQuery(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "搜索信息类型") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                    )
                },
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
            if (uiState.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (uiState.query.isBlank()) "暂无可添加的信息类型" else "没有匹配的信息类型",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    items(
                        items = uiState.items,
                        key = { it.id },
                        contentType = { "CONTENT_TYPE_FIELD_DEFINITION_ITEM" },
                    ) { item ->
                        ItemRow(
                            item = item,
                            onClick = { action(FieldsSelectUserAction.ClickItem(item.id)) },
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ItemRow(
        item: FieldsSelectState.Item,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .singleClick(onClick = onClick)
                .padding(horizontal = 4.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.description.isNotEmpty()) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    companion object {
        private const val EXTRA_DEFINITION_ID = "definition_id"
        private const val ARG_USED_DEFINITION_IDS = "used_definition_ids"

        fun parseResult(intent: Intent?): String? {
            return intent?.getStringExtra(EXTRA_DEFINITION_ID)?.takeIf { it.isNotEmpty() }
        }

        fun newInstance(usedDefinitionIds: List<String>): FieldsSelectFragment {
            return FieldsSelectFragment().apply {
                arguments = bundleOf(ARG_USED_DEFINITION_IDS to ArrayList(usedDefinitionIds))
            }
        }
    }
}