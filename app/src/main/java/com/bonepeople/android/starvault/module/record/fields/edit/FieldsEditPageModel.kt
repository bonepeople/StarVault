package com.bonepeople.android.starvault.module.record.fields.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonepeople.android.base.util.CoroutineExtension.launchOnDefault
import com.bonepeople.android.starvault.global.data.VaultFieldInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class FieldsEditPageModel : ViewModel() {
    val uiState: MutableStateFlow<FieldsEditState> = MutableStateFlow(FieldsEditState.Default)
    private val _effect = Channel<FieldsEditEffect>(capacity = Channel.UNLIMITED)
    val effect = _effect.receiveAsFlow()
    private var initialized = false

    fun init(field: VaultFieldInfo) {
        if (initialized) return
        initialized = true
        val current = field.snapshotList.firstOrNull()
        uiState.update {
            it.copy(
                id = field.id,
                name = current?.name.orEmpty(),
                value = current?.value.orEmpty(),
            )
        }
    }

    fun dispatch(action: FieldsEditUserAction) {
        when (action) {
            is FieldsEditUserAction.UpdateName -> onNameUpdated(action.name)
            is FieldsEditUserAction.UpdateValue -> onValueUpdated(action.value)
            FieldsEditUserAction.ClickBack -> onBackPressed()
        }
    }

    private fun onNameUpdated(name: String) {
        uiState.update { it.copy(name = name, nameError = "") }
    }

    private fun onValueUpdated(value: String) {
        uiState.update { it.copy(value = value, valueError = "") }
    }

    private fun onBackPressed() {
        val name = uiState.value.name.trim()
        val value = uiState.value.value.trim()
        if (uiState.value.id.isEmpty() && name.isEmpty() && value.isEmpty()) {
            finish()
            return
        }
        val nameError = if (name.isEmpty()) "请输入字段名称" else ""
        val valueError = if (value.isEmpty()) "请输入字段值" else ""
        if (nameError.isNotEmpty() || valueError.isNotEmpty()) {
            uiState.update { it.copy(nameError = nameError, valueError = valueError) }
            return
        }
        finish()
    }

    private fun finish() {
        viewModelScope.launchOnDefault {
            _effect.send(FieldsEditEffect.Finish)
        }
    }
}