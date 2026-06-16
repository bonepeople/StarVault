package com.bonepeople.android.starvault.module.home

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bonepeople.android.starvault.global.base.BaseFragment

class HomeFragment : BaseFragment() {
    private val viewModel: HomePageModel by viewModels()

    @Composable
    override fun Content() {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle(viewLifecycleOwner.lifecycle)
        ComposeContent(uiState = uiState, action = viewModel::dispatch)
    }

    override fun initData(savedInstanceState: Bundle?) {
        viewModel.init()
    }

    override fun handleBackPress() {
        requireActivity().finishAndRemoveTask()
    }

    @Preview(showSystemUi = true)
    @Composable
    private fun ComposeContent(uiState: HomeState = HomeState.Preview.Normal, action: (HomeUserAction) -> Unit = {}) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "HomeFragment")
                Button(onClick = { action(HomeUserAction.ClickCreate) }) {
                    Text(text = "创建")
                }
                Button(onClick = { action(HomeUserAction.ClickOpen) }) {
                    Text(text = "打开")
                }
            }
            if (uiState.loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.32f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xE6222222),
                                shape = RoundedCornerShape(12.dp),
                            )
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}