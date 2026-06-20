package com.bonepeople.android.starvault.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

object RecordTag {
    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun TagSection(
        tags: List<String>,
        editable: Boolean,
        modifier: Modifier = Modifier,
        onEdit: () -> Unit = {},
    ) {
        val contentModifier = modifier
            .fillMaxWidth()
            .then(
                if (editable) {
                    Modifier.clickable(onClick = onEdit)
                } else {
                    Modifier
                },
            )
        if (tags.isEmpty()) {
            Text(
                text = if (editable) "点击添加标签" else "无标签",
                modifier = contentModifier,
                style = if (editable) {
                    MaterialTheme.typography.bodyMedium
                } else {
                    MaterialTheme.typography.bodySmall
                },
                color = if (editable) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                },
            )
        } else {
            FlowRow(
                modifier = contentModifier,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                tags.forEach { tag ->
                    TagChip(text = tag)
                }
            }
        }
    }

    @Composable
    private fun TagChip(
        text: String,
        modifier: Modifier = Modifier,
    ) {
        Text(
            text = text,
            modifier = modifier
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
}