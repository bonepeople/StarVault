package com.bonepeople.android.starvault.global.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            handleBackPress()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (view as? ComposeView)?.setContent { Content() }
        initData(savedInstanceState)
    }

    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    protected open fun handleBackPress() {
        onBackPressedCallback.isEnabled = false
        requireActivity().onBackPressedDispatcher.onBackPressed()
        onBackPressedCallback.isEnabled = true
    }

    @Composable
    protected abstract fun Content()

    protected open fun initData(savedInstanceState: Bundle?) {}
}