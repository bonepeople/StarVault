package com.bonepeople.android.starvault.module.record.detail

sealed interface RecordDetailUserAction {
    data class ToggleFieldHistory(val fieldId: String) : RecordDetailUserAction
}
