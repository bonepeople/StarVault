package com.bonepeople.android.starvault.module.record.fields.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bonepeople.android.starvault.global.base.BaseFragment

class FieldsEditFragment : BaseFragment() {
    @Composable
    override fun Content() {
        ComposeContent()
    }

    @Preview(showSystemUi = true)
    @Composable
    private fun ComposeContent() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Text(
                text = "编辑字段",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    companion object {
        fun newInstance(): FieldsEditFragment {
            return FieldsEditFragment()
        }
    }
}