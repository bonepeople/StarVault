package com.bonepeople.android.starvault.module.record.detail

sealed interface RecordDetailUserAction {
    object ClickTags : RecordDetailUserAction
    data class TagsUpdated(val tags: List<String>) : RecordDetailUserAction
    data class ToggleFieldHistory(val fieldId: String) : RecordDetailUserAction
}