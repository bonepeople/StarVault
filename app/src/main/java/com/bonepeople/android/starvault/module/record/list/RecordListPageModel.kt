package com.bonepeople.android.starvault.module.record.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonepeople.android.base.activity.StandardActivity
import com.bonepeople.android.base.util.CoroutineExtension.launchOnDefault
import com.bonepeople.android.starvault.global.VaultManager
import com.bonepeople.android.starvault.module.record.create.RecordCreateFragment
import com.bonepeople.android.starvault.module.record.detail.RecordDetailFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class RecordListPageModel : ViewModel() {
    val uiState: MutableStateFlow<RecordListState> = MutableStateFlow(RecordListState.Default)
    private var initialized = false

    fun init() {
        if (initialized) return
        initialized = true
        loadRecords()
    }

    fun dispatch(action: RecordListUserAction) {
        when (action) {
            RecordListUserAction.ClickCreate -> onCreateClicked()
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

    private fun onCreateClicked() {
        StandardActivity.call(RecordCreateFragment()).onSuccess {
            loadRecords()
        }
    }

    private fun onRecordClicked(recordId: String) {
        StandardActivity.open(RecordDetailFragment.newInstance(recordId))
    }
}