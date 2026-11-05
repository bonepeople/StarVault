package com.bonepeople.android.starvault.module.record.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonepeople.android.base.util.CoroutineExtension.launchOnDefault
import com.bonepeople.android.starvault.global.VaultManager
import com.bonepeople.android.starvault.global.data.VaultFieldInfo
import com.bonepeople.android.starvault.global.data.VaultTagDefinitionInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
            is RecordDetailUserAction.FieldDefinitionSelected -> onFieldDefinitionSelected(action.definitionId)
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
            _effect.send(RecordDetailEffect.OpenFieldsSelect(recordFields.map { it.definitionId }))
        }
    }

    private fun onFieldDefinitionSelected(definitionId: String) {
        if (definitionId.isEmpty()) return
        if (recordFields.any { it.definitionId == definitionId }) return
        if (VaultManager.findFieldDefinitionById(definitionId) == null) return
        viewModelScope.launchOnDefault {
            // TODO: StandardActivity.call会使用正在关闭的页面打开编辑页，添加延迟等待页面完全关闭后再调用，AndroidWidget1.7.7版本会解决
            delay(600)
            _effect.send(RecordDetailEffect.OpenFieldsEdit(VaultFieldInfo(definitionId = definitionId)))
        }
    }

    private fun onFieldClicked(fieldId: String) {
        val field = recordFields.firstOrNull { it.id == fieldId } ?: return
        viewModelScope.launchOnDefault {
            _effect.send(RecordDetailEffect.OpenFieldsEdit(field))
        }
    }

    private fun onTagsUpdated(tags: List<VaultTagDefinitionInfo>) {
        val resolvedTags = VaultManager.updateRecordTags(recordId, tags)
        uiState.update { it.copy(tags = resolvedTags) }
    }

    private fun onFieldSaved(field: VaultFieldInfo) {
        if (field.id.isEmpty() || field.snapshotList.isEmpty()) return
        if (VaultManager.findFieldDefinitionById(field.definitionId) == null) return
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
                    tags = VaultManager.findTagDefinitionsByIds(record.tagDefinitionIdList),
                    fields = recordFields.map { field -> field.toStateField() },
                )
            }
        }
    }

    private fun VaultFieldInfo.toStateField(): RecordDetailState.Field {
        val current = snapshotList.firstOrNull()
        val definitionName = VaultManager.findFieldDefinitionById(definitionId)?.name.orEmpty()
        return RecordDetailState.Field(
            id = id,
            name = definitionName.ifEmpty { "未命名" },
            currentValue = current?.value.orEmpty(),
        )
    }
}