package com.bonepeople.android.starvault.module.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonepeople.android.base.util.CoroutineExtension.launchOnDefault
import com.bonepeople.android.starvault.global.util.DataUtil
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
                var hasViewedGuide = false
                coroutineScope {
                    launch {
                        delay(2000)
                    }
                    launch {
                        // init
                        hasViewedGuide = DataUtil.appStart.getBoolean(DataUtil.Key.AppStart.HAS_VIEWED_GUIDE)
                    }
                }
                pageState.value = if (hasViewedGuide) {
                    PageState.ToHome
                } else {
                    PageState.ToGuide
                }
            }.onFailure {
                pageState.value = PageState.Error
            }
        }
    }

    sealed class PageState {
        object Init : PageState()
        object Loading : PageState()
        object ToGuide : PageState()
        object ToHome : PageState()
        object Error : PageState()
    }
}