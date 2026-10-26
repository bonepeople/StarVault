package com.bonepeople.android.starvault.module.record.fields.definition.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonepeople.android.base.util.CoroutineExtension.launchOnDefault
import com.bonepeople.android.starvault.global.VaultManager
import com.bonepeople.android.starvault.global.util.FieldDefinitionUtil
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class FieldDefinitionEditPageModel : ViewModel() {
    val uiState: MutableStateFlow<FieldDefinitionEditState> = MutableStateFlow(FieldDefinitionEditState.Default)
    private val _effect = Channel<FieldDefinitionEditEffect>(capacity = Channel.UNLIMITED)
    val effect = _effect.receiveAsFlow()
    private var initialized = false

    fun init(definitionId: String) {
        if (initialized) return
        initialized = true
        if (definitionId.isEmpty()) return
        val definition = VaultManager.findFieldDefinitionById(definitionId) ?: return
        uiState.update {
            it.copy(
                name = definition.name,
                description = definition.description,
            )
        }
    }

    fun dispatch(action: FieldDefinitionEditUserAction) {
        when (action) {
            is FieldDefinitionEditUserAction.UpdateName -> onNameUpdated(action.name)
            is FieldDefinitionEditUserAction.UpdateDescription -> onDescriptionUpdated(action.description)
            FieldDefinitionEditUserAction.ClickConfirm -> onConfirmClicked()
        }
    }

    private fun onNameUpdated(name: String) {
        uiState.update { it.copy(name = name, nameError = "") }
    }

    private fun onDescriptionUpdated(description: String) {
        uiState.update { it.copy(description = description) }
    }

    private fun onConfirmClicked() {
        if (uiState.value.saving) return
        val name = uiState.value.name.trim()
        val description = uiState.value.description
        val normalizedName = FieldDefinitionUtil.normalizeName(name)
        val nameError = when {
            name.isEmpty() -> "请输入信息类型名称"
            VaultManager.currentVault.fieldDefinitionList.any { FieldDefinitionUtil.normalizeName(it.name) == normalizedName } -> {
                "信息类型名称已存在"
            }

            else -> ""
        }
        if (nameError.isNotEmpty()) {
            uiState.update { it.copy(nameError = nameError) }
            return
        }
        uiState.update { it.copy(saving = true, nameError = "") }
        viewModelScope.launchOnDefault {
            val definition = VaultManager.createFieldDefinition(name, description)
            _effect.send(FieldDefinitionEditEffect.FinishCreated(definition.id))
        }
    }
}