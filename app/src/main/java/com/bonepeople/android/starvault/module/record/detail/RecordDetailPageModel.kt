package com.bonepeople.android.starvault.module.record.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonepeople.android.base.util.CoroutineExtension.launchOnDefault
import com.bonepeople.android.starvault.global.VaultManager
import com.bonepeople.android.starvault.global.data.VaultFieldInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class RecordDetailPageModel : ViewModel() {
    val uiState: MutableStateFlow<RecordDetailState> = MutableStateFlow(RecordDetailState.Default)
    private val _effect = Channel<RecordDetailEffect>(capacity = Channel.UNLIMITED)
    val effect = _effect.receiveAsFlow()
    private var initialized = false
    private var recordId: String = ""
    private var recordFields: List<VaultFieldInfo> = emptyList()

    fun init(recordId: String) {
        if (initialized) return
        initialized = true
        this.recordId = recordId
        loadRecord()
    }

    fun dispatch(action: RecordDetailUserAction) {
        when (action) {
            RecordDetailUserAction.ClickTags -> onTagsClicked()
            RecordDetailUserAction.ClickAddField -> onAddFieldClicked()
            is RecordDetailUserAction.ClickField -> onFieldClicked(action.fieldId)
            is RecordDetailUserAction.TagsUpdated -> onTagsUpdated(action.tags)
            is RecordDetailUserAction.ToggleFieldHistory -> toggleFieldHistory(action.fieldId)
        }
    }

    private fun onTagsClicked() {
        viewModelScope.launchOnDefault {
            _effect.send(RecordDetailEffect.OpenTagsEdit(uiState.value.tags))
        }
    }

    private fun onAddFieldClicked() {
        viewModelScope.launchOnDefault {
            _effect.send(RecordDetailEffect.OpenFieldsEdit(VaultFieldInfo()))
        }
    }

    private fun onFieldClicked(fieldId: String) {
        val field = recordFields.firstOrNull { it.id == fieldId } ?: return
        viewModelScope.launchOnDefault {
            _effect.send(RecordDetailEffect.OpenFieldsEdit(field))
        }
    }

    private fun onTagsUpdated(tags: List<String>) {
        uiState.update { it.copy(tags = tags) }
        VaultManager.updateRecordTags(recordId, tags)
    }

    private fun loadRecord() {
        viewModelScope.launchOnDefault {
            uiState.update { it.copy(loading = true) }
            val record = VaultManager.findRecordById(recordId)
            if (record == null) {
                recordFields = emptyList()
                uiState.update { it.copy(loading = false, notFound = true) }
                return@launchOnDefault
            }
            recordFields = record.fieldList
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