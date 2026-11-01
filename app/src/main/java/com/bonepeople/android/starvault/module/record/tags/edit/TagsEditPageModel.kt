package com.bonepeople.android.starvault.module.record.tags.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonepeople.android.base.util.CoroutineExtension.launchOnDefault
import com.bonepeople.android.starvault.global.VaultManager
import com.bonepeople.android.starvault.global.data.VaultTagDefinitionInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class TagsEditPageModel : ViewModel() {
    val uiState: MutableStateFlow<TagsEditState> = MutableStateFlow(TagsEditState.Default)
    private val _effect = Channel<TagsEditEffect>(capacity = Channel.UNLIMITED)
    val effect = _effect.receiveAsFlow()
    private var initialized = false

    fun init(tags: List<VaultTagDefinitionInfo>) {
        if (initialized) return
        initialized = true
        uiState.update { it.copy(tags = tags).withUpdatedRecommendations() }
    }

    fun dispatch(action: TagsEditUserAction) {
        when (action) {
            TagsEditUserAction.ClickAdd -> onAddClicked()
            is TagsEditUserAction.TagAdded -> onTagAdded(action.tag)
            is TagsEditUserAction.ClickDelete -> onDelete(action.tag)
            is TagsEditUserAction.MoveTag -> onMoveTag(action.from, action.to)
            TagsEditUserAction.UndoDelete -> onUndoDelete()
            TagsEditUserAction.ClickBack -> onBackPressed()
            is TagsEditUserAction.ClickRecommendTag -> onRecommendTag(action.tag)
        }
    }

    private fun onBackPressed() {
        finishWithCurrentTags()
    }

    private fun onAddClicked() {
        viewModelScope.launchOnDefault {
            _effect.send(TagsEditEffect.OpenTagAdd(uiState.value.tags))
        }
    }

    private fun onTagAdded(tag: VaultTagDefinitionInfo) {
        if (tag.id.isEmpty() || tag.name.isEmpty()) return
        addTag(tag)
    }

    private fun finishWithCurrentTags() {
        viewModelScope.launchOnDefault {
            _effect.send(TagsEditEffect.FinishWithResult(uiState.value.tags))
        }
    }

    private fun onRecommendTag(name: String) {
        if (uiState.value.tags.any { it.name == name }) return
        addTag(VaultTagDefinitionInfo(id = UUID.randomUUID().toString(), name = name))
    }

    private fun addTag(tag: VaultTagDefinitionInfo) {
        uiState.update { state ->
            if (state.tags.any { it.id == tag.id }) {
                return@update state
            }
            state.copy(tags = state.tags + tag).withUpdatedRecommendations()
        }
    }

    private fun onDelete(tag: VaultTagDefinitionInfo) {
        val index = uiState.value.tags.indexOfFirst { it.id == tag.id }
        if (index < 0) return
        uiState.update {
            it.copy(
                tags = it.tags.filterIndexed { i, _ -> i != index },
                pendingUndo = PendingUndoInfo(tag = tag, index = index),
            ).withUpdatedRecommendations()
        }
    }

    private fun onMoveTag(from: Int, to: Int) {
        uiState.update { state ->
            val tags = state.tags
            if (from !in tags.indices || to !in tags.indices || from == to) {
                return@update state
            }
            val newTags = tags.toMutableList().apply { add(to, removeAt(from)) }
            state.copy(tags = newTags)
        }
    }

    private fun onUndoDelete() {
        val undo = uiState.value.pendingUndo ?: return
        uiState.update { state ->
            val newTags = state.tags.filter { it.id != undo.tag.id }.toMutableList()
            val insertIndex = undo.index.coerceIn(0, newTags.size)
            newTags.add(insertIndex, undo.tag)
            state.copy(tags = newTags, pendingUndo = null).withUpdatedRecommendations()
        }
    }

    private fun TagsEditState.withUpdatedRecommendations(): TagsEditState {
        val selectedNames = tags.map { it.name }.toSet()
        return copy(recommendedTags = VaultManager.recommendedTagList.filter { it !in selectedNames })
    }
}