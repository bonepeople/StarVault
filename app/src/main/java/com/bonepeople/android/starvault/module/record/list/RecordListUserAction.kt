package com.bonepeople.android.starvault.module.record.list

sealed interface RecordListUserAction {
    data class ClickItem(val recordId: String) : RecordListUserAction
}