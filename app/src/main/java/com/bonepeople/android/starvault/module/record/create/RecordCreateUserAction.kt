package com.bonepeople.android.starvault.module.record.create

sealed interface RecordCreateUserAction {
    data class UpdateTitle(val title: String) : RecordCreateUserAction
    object ClickTags : RecordCreateUserAction
    data class TagsUpdated(val tags: List<String>) : RecordCreateUserAction
    object ClickNext : RecordCreateUserAction
}