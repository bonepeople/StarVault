package com.bonepeople.android.starvault.module.record.fields.definition.manage

import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bonepeople.android.base.activity.StandardActivity
import com.bonepeople.android.base.util.FlowExtension.observeWithLifecycle
import com.bonepeople.android.starvault.global.base.BaseFragment
import com.bonepeople.android.starvault.global.util.ClickExtension
import com.bonepeople.android.starvault.module.record.fields.definition.edit.FieldDefinitionEditFragment

class FieldDefinitionManageFragment : BaseFragment() {
    private val viewModel: FieldDefinitionManagePageModel by viewModels()

    @Composable
    override fun Content() {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle(viewLifecycleOwner.lifecycle)
        ComposeContent(uiState = uiState, action = viewModel::dispatch)
    }

    override fun initData(savedInstanceState: Bundle?) {
        viewModel.effect.observeWithLifecycle(viewLifecycleOwner, Lifecycle.State.RESUMED) { effect ->
            when (effect) {
                FieldDefinitionManageEffect.OpenDefinitionCreate -> openDefinitionCreate()
            }
        }
        viewModel.init()
    }

    private fun openDefinitionCreate() {
        StandardActivity.call(FieldDefinitionEditFragment.newInstance(definitionId = "")).onSuccess { intent ->
            val definitionId = FieldDefinitionEditFragment.parseResult(intent) ?: return@onSuccess
            viewModel.dispatch(FieldDefinitionManageUserAction.DefinitionCreated(definitionId))
        }
    }

    @Preview(showSystemUi = true)
    @Composable
    private fun ComposeContent(
        uiState: FieldDefinitionManageState = FieldDefinitionManageState.Preview.Normal,
        action: (FieldDefinitionManageUserAction) -> Unit = {},
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Text(
                text = "管理信息类型",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = ClickExtension.rememberSingleClick {
                    action(FieldDefinitionManageUserAction.ClickCreateDefinition)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                )
                Spacer(Modifier.width(8.dp))
                Text(text = "新建信息类型")
            }
            Spacer(Modifier.height(8.dp))
            if (uiState.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "暂无信息类型",
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
                        ItemRow(item = item)
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
        item: FieldDefinitionManageState.Item,
        modifier: Modifier = Modifier,
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
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
            Text(
                text = "使用 ${item.usageCount} 次",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}