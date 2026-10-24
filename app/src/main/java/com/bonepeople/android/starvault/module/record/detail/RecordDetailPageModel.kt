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
            is RecordDetailUserAction.FieldSaved -> onFieldSaved(action.field)
            is RecordDetailUserAction.FieldDeleted -> onFieldDeleted(action.fieldId)
            is RecordDetailUserAction.TagsUpdated -> onTagsUpdated(action.tags)
            RecordDetailUserAction.ClickDelete -> onDeleteClicked()
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

    private fun onFieldSaved(field: VaultFieldInfo) {
        if (field.id.isEmpty() || field.snapshotList.isEmpty()) return
        val fieldExists = recordFields.any { it.id == field.id }
        recordFields = if (fieldExists) {
            recordFields.map { if (it.id == field.id) field else it }
        } else {
            recordFields + field
        }
        uiState.update { it.copy(fields = recordFields.map { item -> item.toStateField() }) }
        VaultManager.updateRecordFields(recordId, recordFields)
    }

    private fun onDeleteClicked() {
        if (VaultManager.findRecordById(recordId) == null) return
        VaultManager.deleteRecord(recordId)
        viewModelScope.launchOnDefault {
            _effect.send(RecordDetailEffect.FinishDeleted)
        }
    }

    private fun onFieldDeleted(fieldId: String) {
        if (recordFields.none { it.id == fieldId }) return
        recordFields = recordFields.filterNot { it.id == fieldId }
        uiState.update { it.copy(fields = recordFields.map { item -> item.toStateField() }) }
        VaultManager.deleteRecordField(recordId, fieldId)
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
            uiState.update {
                it.copy(
                    loading = false,
                    title = record.title,
                    tags = record.tagList,
                    fields = recordFields.map { field -> field.toStateField() },
                )
            }
        }
    }

    private fun VaultFieldInfo.toStateField(): RecordDetailState.Field {
        val current = snapshotList.firstOrNull()
        return RecordDetailState.Field(
            id = id,
            name = name.ifEmpty { "未命名" },
            currentValue = current?.value.orEmpty(),
        )
    }
}