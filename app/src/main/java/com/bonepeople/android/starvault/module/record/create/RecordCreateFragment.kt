package com.bonepeople.android.starvault.module.record.create

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bonepeople.android.starvault.global.base.BaseFragment

class RecordCreateFragment : BaseFragment() {
    @Composable
    override fun Content() {
        ComposeContent()
    }

    @Preview(showSystemUi = true)
    @Composable
    private fun ComposeContent() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "新建页面")
        }
    }
}