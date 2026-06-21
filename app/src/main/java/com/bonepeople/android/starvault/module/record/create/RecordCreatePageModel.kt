package com.bonepeople.android.starvault.module.record.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bonepeople.android.base.util.CoroutineExtension.launchOnDefault
import com.bonepeople.android.starvault.global.VaultManager
import com.bonepeople.android.starvault.global.data.VaultRecordInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class RecordCreatePageModel : ViewModel() {
    val uiState: MutableStateFlow<RecordCreateState> = MutableStateFlow(RecordCreateState.Default)
    private val _effect = Channel<RecordCreateEffect>(capacity = Channel.UNLIMITED)
    val effect = _effect.receiveAsFlow()

    fun dispatch(action: RecordCreateUserAction) {
        when (action) {
            is RecordCreateUserAction.UpdateTitle -> onTitleUpdated(action.title)
            RecordCreateUserAction.ClickTags -> onTagsClicked()
            RecordCreateUserAction.ClickNext -> onNextClicked()
        }
    }

    private fun onTitleUpdated(title: String) {
        uiState.update { it.copy(title = title, titleError = "") }
    }

    // TODO: 步骤3
    private fun onTagsClicked() {
    }

    private fun onNextClicked() {
        viewModelScope.launchOnDefault {
            val title = uiState.value.title.trim()
            if (title.isEmpty()) {
                uiState.update { it.copy(titleError = "请输入名称") }
                return@launchOnDefault
            }
            uiState.update { it.copy(saving = true) }
            val record = VaultRecordInfo(
                id = UUID.randomUUID().toString(),
                title = title,
                tagList = uiState.value.tags,
                fieldList = emptyList(),
            )
            VaultManager.addRecord(record)
            _effect.send(RecordCreateEffect.OpenDetail(record.id))
        }
    }
}