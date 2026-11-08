package com.bonepeople.android.starvault.module.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonepeople.android.base.activity.StandardActivity
import com.bonepeople.android.base.util.CoroutineExtension.launchOnDefault
import com.bonepeople.android.starvault.global.VaultManager
import com.bonepeople.android.starvault.module.record.list.RecordListFragment
import com.bonepeople.android.starvault.module.test.TestFragment
import com.bonepeople.android.starvault.module.vault.create.VaultCreateFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class HomePageModel : ViewModel() {
    val uiState: MutableStateFlow<HomeState> = MutableStateFlow(HomeState.Default)
    private var initialized = false

    fun init() {
        if (initialized) return
        initialized = true
    }

    fun dispatch(action: HomeUserAction) {
        when (action) {
            HomeUserAction.ClickCreate -> createVault()
            HomeUserAction.ClickOpen -> openVault()
            HomeUserAction.ClickTest -> StandardActivity.open(TestFragment())
        }
    }

    fun createVault() {
        StandardActivity.open(VaultCreateFragment())
    }

    fun openVault() {
        viewModelScope.launchOnDefault {
            uiState.update { it.copy(loading = true) }
            VaultManager.generateFakeVault()
            delay(500)
            StandardActivity.open(RecordListFragment())
            uiState.update { it.copy(loading = false) }
        }
    }
}