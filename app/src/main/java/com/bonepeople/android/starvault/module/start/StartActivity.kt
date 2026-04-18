package com.bonepeople.android.starvault.module.start

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import com.bonepeople.android.base.activity.StandardActivity
import com.bonepeople.android.base.util.FlowExtension.observeWithLifecycle
import com.bonepeople.android.starvault.module.guide.GuideFragment
import com.gyf.immersionbar.ktx.immersionBar

class StartActivity : ComponentActivity() {
    private val viewModel: StartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersionBar {
            transparentBar()
            statusBarDarkFont(true)
            navigationBarDarkIcon(true)
        }
        setContent { ComposeContent() }
        viewModel.pageState.observeWithLifecycle(this) { pageState ->
            if (pageState is StartViewModel.PageState.Finish) {
                StandardActivity.open(GuideFragment())
                finishAfterTransition()
            }
        }
        viewModel.init()
    }

    @Preview(showSystemUi = true)
    @Composable
    private fun ComposeContent() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Text(
                text = "StartActivity",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}