package com.bonepeople.android.starvault.module.record.create

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class RecordCreatePageModel : ViewModel() {
    val uiState: MutableStateFlow<RecordCreateState> = MutableStateFlow(RecordCreateState.Default)

    fun dispatch(action: RecordCreateUserAction) {
        when (action) {
            is RecordCreateUserAction.UpdateTitle -> onTitleUpdated(action.title)
            RecordCreateUserAction.ClickTags -> onTagsClicked()
            RecordCreateUserAction.ClickNext -> onNextClicked()
        }
    }

    private fun onTitleUpdated(title: String) {
        uiState.update { it.copy(title = title) }
    }

    // TODO: 步骤3
    private fun onTagsClicked() {
    }

    // TODO: 步骤2
    private fun onNextClicked() {
    }
}