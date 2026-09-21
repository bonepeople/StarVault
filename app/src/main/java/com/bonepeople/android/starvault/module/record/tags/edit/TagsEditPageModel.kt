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
            TagsEditUserAction.ClickOpenAddDialog -> onOpenAddDialog()
            TagsEditUserAction.DismissAddDialog -> onDismissAddDialog()
            is TagsEditUserAction.UpdateInput -> onInputUpdated(action.text)
            TagsEditUserAction.ClickConfirmAdd -> onConfirmAdd()
            is TagsEditUserAction.ClickDelete -> onDelete(action.tag)
            TagsEditUserAction.UndoDelete -> onUndoDelete()
            TagsEditUserAction.ClickBack -> onBackPressed()
            is TagsEditUserAction.ClickRecommendTag -> onRecommendTag(action.tag)
        }
    }

    private fun onBackPressed() {
        if (uiState.value.addDialogVisible) {
            onDismissAddDialog()
        } else {
            finishWithCurrentTags()
        }
    }

    private fun finishWithCurrentTags() {
        viewModelScope.launchOnDefault {
            _effect.send(TagsEditEffect.FinishWithResult(uiState.value.tags))
        }
    }

    private fun onOpenAddDialog() {
        uiState.update {
            it.copy(
                addDialogVisible = true,
                inputText = "",
                inputError = "",
            )
        }
    }

    private fun onDismissAddDialog() {
        uiState.update {
            it.copy(
                addDialogVisible = false,
                inputText = "",
                inputError = "",
            )
        }
    }

    private fun onInputUpdated(text: String) {
        uiState.update { it.copy(inputText = text, inputError = "") }
    }

    private fun onConfirmAdd() {
        val tag = uiState.value.inputText.trim()
        if (tag.isEmpty()) {
            return
        }
        if (tag in uiState.value.tags) {
            uiState.update { it.copy(inputError = "标签已存在") }
            return
        }
        addTag(tag)
    }

    private fun onRecommendTag(tag: String) {
        if (tag in uiState.value.tags) {
            return
        }
        addTag(tag)
    }

    private fun addTag(tag: String) {
        uiState.update {
            it.copy(
                tags = it.tags + tag,
                addDialogVisible = false,
                inputText = "",
                inputError = "",
            ).withUpdatedRecommendations()
        }
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