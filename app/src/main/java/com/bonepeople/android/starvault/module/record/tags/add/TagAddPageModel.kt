package com.bonepeople.android.starvault.module.record.tags.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonepeople.android.base.util.CoroutineExtension.launchOnDefault
import com.bonepeople.android.starvault.global.data.VaultTagDefinitionInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class TagAddPageModel : ViewModel() {
    val uiState: MutableStateFlow<TagAddState> = MutableStateFlow(TagAddState.Default)
    private val _effect = Channel<TagAddEffect>(capacity = Channel.UNLIMITED)
    val effect = _effect.receiveAsFlow()
    private var initialized = false
    private var existingTagNames: Set<String> = emptySet()

    fun init(existingTags: List<VaultTagDefinitionInfo>) {
        if (initialized) return
        initialized = true
        existingTagNames = existingTags.map { it.name }.toSet()
    }

    fun dispatch(action: TagAddUserAction) {
        when (action) {
            is TagAddUserAction.UpdateName -> onNameUpdated(action.name)
            TagAddUserAction.ClickConfirm -> onConfirmClicked()
        }
    }

    private fun onNameUpdated(name: String) {
        uiState.update { it.copy(name = name, nameError = "") }
    }

    private fun onConfirmClicked() {
        if (uiState.value.submitting) return
        val name = uiState.value.name.trim()
        val nameError = when {
            name.isEmpty() -> "请输入标签"
            name in existingTagNames -> "标签已存在"
            else -> ""
        }
        if (nameError.isNotEmpty()) {
            uiState.update { it.copy(nameError = nameError) }
            return
        }
        uiState.update { it.copy(submitting = true, nameError = "") }
        val tag = VaultTagDefinitionInfo(id = UUID.randomUUID().toString(), name = name)
        viewModelScope.launchOnDefault {
            _effect.send(TagAddEffect.FinishAdded(tag))
        }
    }
}