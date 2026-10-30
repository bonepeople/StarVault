package com.bonepeople.android.starvault.module.record.tags.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonepeople.android.base.util.CoroutineExtension.launchOnDefault
import com.bonepeople.android.starvault.global.VaultManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class TagsEditPageModel : ViewModel() {
    val uiState: MutableStateFlow<TagsEditState> = MutableStateFlow(TagsEditState.Default)
    private val _effect = Channel<TagsEditEffect>(capacity = Channel.UNLIMITED)
    val effect = _effect.receiveAsFlow()
    private var initialized = false

    fun init(tags: List<String>) {
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

    private fun onTagAdded(tag: String) {
        val name = tag.trim()
        if (name.isEmpty() || name in uiState.value.tags) return
        addTag(name)
    }

    private fun finishWithCurrentTags() {
        viewModelScope.launchOnDefault {
            _effect.send(TagsEditEffect.FinishWithResult(uiState.value.tags))
        }
    }

    private fun onRecommendTag(tag: String) {
        if (tag in uiState.value.tags) {
            return
        }
        addTag(tag)
    }

    private fun addTag(tag: String) {
        uiState.update { it.copy(tags = it.tags + tag).withUpdatedRecommendations() }
    }

    private fun onDelete(tag: String) {
        val index = uiState.value.tags.indexOf(tag)
        if (index < 0) {
            return
        }
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
            val newTags = state.tags.filter { it != undo.tag }.toMutableList()
            val insertIndex = undo.index.coerceIn(0, newTags.size)
            newTags.add(insertIndex, undo.tag)
            state.copy(tags = newTags, pendingUndo = null).withUpdatedRecommendations()
        }
    }

    private fun TagsEditState.withUpdatedRecommendations(): TagsEditState {
        return copy(recommendedTags = VaultManager.recommendedTagList.filter { it !in tags })
    }
}