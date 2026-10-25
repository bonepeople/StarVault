package com.bonepeople.android.starvault.module.record.fields.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonepeople.android.base.util.CoroutineExtension.launchOnDefault
import com.bonepeople.android.starvault.global.VaultManager
import com.bonepeople.android.starvault.global.data.VaultFieldInfo
import com.bonepeople.android.starvault.global.data.VaultFieldSnapshotInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class FieldsEditPageModel : ViewModel() {
    val uiState: MutableStateFlow<FieldsEditState> = MutableStateFlow(FieldsEditState.Default)
    private val _effect = Channel<FieldsEditEffect>(capacity = Channel.UNLIMITED)
    val effect = _effect.receiveAsFlow()
    private var initialized = false
    private var initialField: VaultFieldInfo = VaultFieldInfo()

    fun init(field: VaultFieldInfo) {
        if (initialized) return
        val definition = VaultManager.findFieldDefinitionById(field.definitionId) ?: return
        initialized = true
        initialField = field
        val current = field.snapshotList.firstOrNull()
        uiState.update {
            it.copy(
                id = field.id,
                name = definition.name,
                description = definition.description,
                value = current?.value.orEmpty(),
                history = field.snapshotList.drop(1).map { snapshot ->
                    FieldsEditState.HistoryItem(
                        value = snapshot.value,
                        createTimestamp = snapshot.createTimestamp,
                    )
                },
            )
        }
        viewModelScope.launchOnDefault {
            _effect.send(FieldsEditEffect.RequestFocus(FieldsEditEffect.FocusTarget.Value))
        }
    }

    fun dispatch(action: FieldsEditUserAction) {
        when (action) {
            is FieldsEditUserAction.UpdateValue -> onValueUpdated(action.value)
            FieldsEditUserAction.ClickConfirm -> onConfirmClicked()
            FieldsEditUserAction.ClickDelete -> onDeleteClicked()
        }
    }

    private fun onValueUpdated(value: String) {
        uiState.update { it.copy(value = value, valueError = "") }
    }

    private fun onConfirmClicked() {
        val value = uiState.value.value.trim()
        val valueError = if (value.isEmpty()) "请输入内容" else ""
        if (valueError.isNotEmpty()) {
            uiState.update { it.copy(valueError = valueError) }
            return
        }
        val current = initialField.snapshotList.firstOrNull()
        val snapshotList = if (current != null && current.value == value) {
            initialField.snapshotList
        } else {
            val snapshot = VaultFieldSnapshotInfo(value = value, createTimestamp = System.currentTimeMillis())
            listOf(snapshot) + initialField.snapshotList
        }
        val field = VaultFieldInfo(
            id = uiState.value.id.ifEmpty { UUID.randomUUID().toString() },
            definitionId = initialField.definitionId,
            snapshotList = snapshotList,
        )
        finishSaved(field)
    }

    private fun finishSaved(field: VaultFieldInfo) {
        viewModelScope.launchOnDefault {
            _effect.send(FieldsEditEffect.FinishSaved(field))
        }
    }

    private fun onDeleteClicked() {
        val fieldId = uiState.value.id
        if (fieldId.isEmpty()) return
        viewModelScope.launchOnDefault {
            _effect.send(FieldsEditEffect.FinishDeleted(fieldId))
        }
    }
}