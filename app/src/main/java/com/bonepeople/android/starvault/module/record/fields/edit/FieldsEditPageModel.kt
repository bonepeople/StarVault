package com.bonepeople.android.starvault.module.record.fields.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonepeople.android.base.util.CoroutineExtension.launchOnDefault
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
        initialized = true
        initialField = field
        val current = field.snapshotList.firstOrNull()
        uiState.update {
            it.copy(
                id = field.id,
                name = current?.name.orEmpty(),
                value = current?.value.orEmpty(),
                history = field.snapshotList.drop(1).map { snapshot ->
                    FieldsEditState.HistoryItem(
                        value = snapshot.value,
                        createTimestamp = snapshot.createTimestamp,
                    )
                },
            )
        }
    }

    fun dispatch(action: FieldsEditUserAction) {
        when (action) {
            is FieldsEditUserAction.UpdateName -> onNameUpdated(action.name)
            is FieldsEditUserAction.UpdateValue -> onValueUpdated(action.value)
            FieldsEditUserAction.ClickConfirm -> onConfirmClicked()
        }
    }

    private fun onNameUpdated(name: String) {
        uiState.update { it.copy(name = name, nameError = "") }
    }

    private fun onValueUpdated(value: String) {
        uiState.update { it.copy(value = value, valueError = "") }
    }

    private fun onConfirmClicked() {
        val name = uiState.value.name.trim()
        val value = uiState.value.value.trim()
        val nameError = if (name.isEmpty()) "请输入字段名称" else ""
        val valueError = if (value.isEmpty()) "请输入字段值" else ""
        if (nameError.isNotEmpty() || valueError.isNotEmpty()) {
            uiState.update { it.copy(nameError = nameError, valueError = valueError) }
            return
        }
        val snapshot = VaultFieldSnapshotInfo(
            name = name,
            value = value,
            createTimestamp = System.currentTimeMillis(),
        )
        val field = VaultFieldInfo(
            id = uiState.value.id.ifEmpty { UUID.randomUUID().toString() },
            snapshotList = listOf(snapshot) + initialField.snapshotList,
        )
        finishSaved(field)
    }

    private fun finishSaved(field: VaultFieldInfo) {
        viewModelScope.launchOnDefault {
            _effect.send(FieldsEditEffect.FinishSaved(field))
        }
    }
}