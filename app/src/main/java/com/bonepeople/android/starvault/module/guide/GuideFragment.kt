package com.bonepeople.android.starvault.module.guide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bonepeople.android.base.activity.StandardActivity
import com.bonepeople.android.starvault.global.util.DataUtil
import com.bonepeople.android.starvault.module.home.HomeFragment
import kotlinx.coroutines.launch

class GuideFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { ComposeContent() }
        }
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