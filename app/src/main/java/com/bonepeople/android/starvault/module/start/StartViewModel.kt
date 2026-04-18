package com.bonepeople.android.starvault.module.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonepeople.android.base.util.CoroutineExtension.launchOnDefault
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class StartViewModel : ViewModel() {
    val pageState: MutableStateFlow<PageState> = MutableStateFlow(PageState.Init)

    fun init() {
        viewModelScope.launchOnDefault {
            runCatching {
                pageState.value = PageState.Loading
                coroutineScope {
                    launch {
                        delay(2000)
                    }
                    launch {
                        // init
                    }
                }
            }.onSuccess {
                pageState.value = PageState.Finish
            }.onFailure {
                pageState.value = PageState.Error
            }
        }
    }

    sealed class PageState {
        object Init : PageState()
        object Loading : PageState()
        object Error : PageState()
        object Finish : PageState()
    }
}