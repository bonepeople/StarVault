package com.bonepeople.android.starvault.module.record.fields.definition.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonepeople.android.base.util.CoroutineExtension.launchOnDefault
import com.bonepeople.android.starvault.global.VaultManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class FieldDefinitionManagePageModel : ViewModel() {
    val uiState: MutableStateFlow<FieldDefinitionManageState> = MutableStateFlow(FieldDefinitionManageState.Default)
    private val _effect = Channel<FieldDefinitionManageEffect>(capacity = Channel.UNLIMITED)
    val effect = _effect.receiveAsFlow()
    private var initialized = false
    private var loadItemsJob: Job? = null

    fun init() {
        if (initialized) return
        initialized = true
        loadItems()
    }

    fun dispatch(action: FieldDefinitionManageUserAction) {
        when (action) {
            FieldDefinitionManageUserAction.ClickCreateDefinition -> onCreateDefinitionClicked()
            is FieldDefinitionManageUserAction.DefinitionCreated -> onDefinitionCreated(action.definitionId)
        }
    }

    private fun onCreateDefinitionClicked() {
        viewModelScope.launchOnDefault {
            _effect.send(FieldDefinitionManageEffect.OpenDefinitionCreate)
        }
    }

    private fun onDefinitionCreated(definitionId: String) {
        if (VaultManager.findFieldDefinitionById(definitionId) == null) return
        loadItems()
    }

    private fun loadItems() {
        loadItemsJob?.cancel()
        loadItemsJob = viewModelScope.launchOnDefault {
            val usageCountByDefinitionId = VaultManager.currentVault.recordList
                .asSequence()
                .flatMap { it.fieldList.asSequence() }
                .groupingBy { it.definitionId }
                .eachCount()
            val items = VaultManager.currentVault.fieldDefinitionList.map { definition ->
                FieldDefinitionManageState.Item(
                    id = definition.id,
                    name = definition.name,
                    description = definition.description,
                    usageCount = usageCountByDefinitionId[definition.id] ?: 0,
                )
            }
            uiState.update { it.copy(items = items) }
        }
    }
}