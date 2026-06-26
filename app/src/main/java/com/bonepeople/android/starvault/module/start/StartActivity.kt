package com.bonepeople.android.starvault.module.start

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bonepeople.android.base.activity.StandardActivity
import com.bonepeople.android.base.util.FlowExtension.observeWithLifecycle
import com.bonepeople.android.starvault.R
import com.bonepeople.android.starvault.module.guide.GuideFragment
import com.bonepeople.android.starvault.module.home.HomeFragment
import com.bonepeople.android.widget.ApplicationHolder
import com.gyf.immersionbar.ktx.immersionBar

class StartActivity : ComponentActivity() {
    private val viewModel: StartPageModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersionBar {
            transparentBar()
            statusBarDarkFont(true)
            navigationBarDarkIcon(true)
        }
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            ComposeContent(uiState = uiState)
        }
        viewModel.effect.observeWithLifecycle(this, Lifecycle.State.RESUMED) { effect ->
            when (effect) {
                StartEffect.NavigateToGuide -> {
                    StandardActivity.open(GuideFragment())
                    finishAfterTransition()
                }

                StartEffect.NavigateToHome -> {
                    StandardActivity.open(HomeFragment())
                    finishAfterTransition()
                }
            }
        }
        viewModel.init(ApplicationHolder.getVersionName())
    }

    @Preview(showSystemUi = true)
    @Composable
    private fun ComposeContent(uiState: StartState = StartState.Preview.Normal) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (uiState.error) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "启动失败",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            if (uiState.appVersion.isNotEmpty()) {
                Text(
                    text = "v${uiState.appVersion}",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
    }
}