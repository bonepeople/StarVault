package com.bonepeople.android.starvault.module.record.fields.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonepeople.android.base.util.CoroutineExtension.launchOnDefault
import com.bonepeople.android.starvault.global.VaultManager
import com.bonepeople.android.starvault.global.util.FieldDefinitionUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class FieldsSelectPageModel : ViewModel() {
    val uiState: MutableStateFlow<FieldsSelectState> = MutableStateFlow(FieldsSelectState.Default)
    private val _effect = Channel<FieldsSelectEffect>(capacity = Channel.UNLIMITED)
    val effect = _effect.receiveAsFlow()
    private var initialized = false
    private var availableItems: List<FieldsSelectState.Item> = emptyList()
    private var updateItemsJob: Job? = null

    fun init(usedDefinitionIds: List<String>) {
        if (initialized) return
        initialized = true
        viewModelScope.launchOnDefault {
            val usedDefinitionIdSet = usedDefinitionIds.toSet()
            val usageCountByDefinitionId = VaultManager.currentVault.recordList
                .asSequence()
                .flatMap { it.fieldList.asSequence() }
                .groupingBy { it.definitionId }
                .eachCount()
            availableItems = VaultManager.currentVault.fieldDefinitionList
                .asSequence()
                .filterNot { it.id in usedDefinitionIdSet }
                .map { definition ->
                    FieldsSelectState.Item(
                        id = definition.id,
                        name = definition.name,
                        description = definition.description,
                        usageCount = usageCountByDefinitionId[definition.id] ?: 0,
                    )
                }
                .sortedWith(
                    compareByDescending<FieldsSelectState.Item> { it.usageCount }
                        .thenBy { FieldDefinitionUtil.normalizeName(it.name) }
                        .thenBy { it.id },
                )
                .toList()
            uiState.update { it.copy(items = availableItems) }
        }
    }

    fun dispatch(action: FieldsSelectUserAction) {
        when (action) {
            is FieldsSelectUserAction.UpdateQuery -> onQueryUpdated(action.query)
            is FieldsSelectUserAction.ClickItem -> onItemClicked(action.definitionId)
        }
    }

    private fun onItemClicked(definitionId: String) {
        viewModelScope.launchOnDefault {
            _effect.send(FieldsSelectEffect.FinishSelected(definitionId))
        }
    }

    private fun onQueryUpdated(query: String) {
        uiState.update { it.copy(query = query) }
        updateItemsJob?.cancel()
        updateItemsJob = viewModelScope.launchOnDefault {
            val items = filterItems(query)
            coroutineContext.ensureActive()
            uiState.update { it.copy(items = items) }
        }
    }

    private fun filterItems(query: String): List<FieldsSelectState.Item> {
        val normalizedQuery = FieldDefinitionUtil.normalizeName(query)
        if (normalizedQuery.isEmpty()) return availableItems
        return availableItems.filter { item ->
            FieldDefinitionUtil.normalizeName(item.name).contains(normalizedQuery) || item.description.contains(normalizedQuery, ignoreCase = true)
        }
    }
}