package com.bonepeople.android.starvault.module.record.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonepeople.android.base.util.CoroutineExtension.launchOnDefault
import com.bonepeople.android.starvault.global.VaultManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class RecordListPageModel : ViewModel() {
    val uiState: MutableStateFlow<RecordListState> = MutableStateFlow(RecordListState())
    private var initialized = false

    fun init() {
        if (initialized) return
        initialized = true
        loadRecords()
    }

    fun dispatch(action: RecordListUserAction) {
        when (action) {
            is RecordListUserAction.ClickItem -> onRecordClicked(action.recordId)
        }
    }

    private fun loadRecords() {
        viewModelScope.launchOnDefault {
            uiState.update { it.copy(loading = true) }
            val items = VaultManager.currentVault.recordList.map { record ->
                RecordListState.Item(
                    id = record.id,
                    title = record.title,
                    tags = record.tagList,
                )
            }
            uiState.update { it.copy(loading = false, items = items) }
        }
    }

    private fun onRecordClicked(recordId: String) {
        // TODO: 打开记录详情
    }
}