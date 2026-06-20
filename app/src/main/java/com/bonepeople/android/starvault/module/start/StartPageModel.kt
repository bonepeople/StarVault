package com.bonepeople.android.starvault.module.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonepeople.android.base.util.CoroutineExtension.launchOnDefault
import com.bonepeople.android.starvault.global.util.DataUtil
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StartPageModel : ViewModel() {
    val uiState: MutableStateFlow<StartState> = MutableStateFlow(StartState.Default)
    private val _effect = Channel<StartEffect>(capacity = Channel.UNLIMITED)
    val effect = _effect.receiveAsFlow()
    private var initialized = false

    fun init(appVersion: String) {
        if (initialized) return
        initialized = true
        uiState.update { it.copy(appVersion = appVersion) }
        load()
    }

    private fun load() {
        viewModelScope.launchOnDefault {
            runCatching {
                var hasViewedGuide = false
                coroutineScope {
                    launch {
                        delay(1000)
                    }
                    launch {
                        hasViewedGuide = DataUtil.appStart.getBoolean(DataUtil.Key.AppStart.HAS_VIEWED_GUIDE)
                    }
                }
                val effect = if (hasViewedGuide) {
                    StartEffect.NavigateToHome
                } else {
                    StartEffect.NavigateToGuide
                }
                _effect.send(effect)
            }.onFailure {
                uiState.update { it.copy(error = true) }
            }
        }
    }
}