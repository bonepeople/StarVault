package com.bonepeople.android.starvault.module.record.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonepeople.android.base.util.CoroutineExtension.launchOnDefault
import com.bonepeople.android.starvault.global.VaultManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class RecordDetailPageModel : ViewModel() {
    val uiState: MutableStateFlow<RecordDetailState> = MutableStateFlow(RecordDetailState.Default)
    private var initialized = false
    private var recordId: String = ""

    fun init(recordId: String) {
        if (initialized) return
        initialized = true
        this.recordId = recordId
        loadRecord()
    }

    fun dispatch(action: RecordDetailUserAction) {
        when (action) {
            RecordDetailUserAction.ClickTags -> onTagsClicked()
            is RecordDetailUserAction.ToggleFieldHistory -> toggleFieldHistory(action.fieldId)
        }
    }

    private fun onTagsClicked() {

    }

    private fun loadRecord() {
        viewModelScope.launchOnDefault {
            uiState.update { it.copy(loading = true) }
            val record = VaultManager.findRecordById(recordId)
            if (record == null) {
                uiState.update { it.copy(loading = false, notFound = true) }
                return@launchOnDefault
            }
            val fields = record.fieldList.map { field ->
                val snapshots = field.snapshotList
                val current = snapshots.firstOrNull()
                val history = snapshots.drop(1).map { snapshot ->
                    RecordDetailState.Field.HistoryItem(
                        value = snapshot.value,
                        createTimestamp = snapshot.createTimestamp,
                    )
                }
                RecordDetailState.Field(
                    id = field.id,
                    name = current?.name.orEmpty().ifEmpty { "未命名" },
                    currentValue = current?.value.orEmpty(),
                    history = history,
                )
            }
            uiState.update {
                it.copy(
                    loading = false,
                    title = record.title,
                    tags = record.tagList,
                    fields = fields,
                )
            }
        }
    }

    private fun toggleFieldHistory(fieldId: String) {
        uiState.update { state ->
            state.copy(
                fields = state.fields.map { field ->
                    if (field.id == fieldId) {
                        field.copy(historyExpanded = !field.historyExpanded)
                    } else {
                        field
                    }
                },
            )
        }
    }
}
