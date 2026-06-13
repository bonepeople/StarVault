package com.bonepeople.android.starvault.module.guide

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.bonepeople.android.base.activity.StandardActivity
import com.bonepeople.android.starvault.global.base.BaseFragment
import com.bonepeople.android.starvault.global.util.DataUtil
import com.bonepeople.android.starvault.module.home.HomeFragment
import kotlinx.coroutines.launch

class GuideFragment : BaseFragment() {
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
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "GuideFragment")
                Button(
                    onClick = {
                        viewLifecycleOwner.lifecycleScope.launch {
                            DataUtil.appStart.putBoolean(DataUtil.Key.AppStart.HAS_VIEWED_GUIDE, true)
                            StandardActivity.open(HomeFragment())
                            activity?.finishAfterTransition()
                        }
                    }
                ) {
                    Text(text = "Open HomeFragment")
                }
            }
        }
    }
}