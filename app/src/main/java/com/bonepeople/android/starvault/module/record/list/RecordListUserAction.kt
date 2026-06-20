package com.bonepeople.android.starvault.module.record.list

sealed interface RecordListUserAction {
    object ClickCreate : RecordListUserAction
    data class ClickItem(val recordId: String) : RecordListUserAction
}