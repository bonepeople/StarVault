package com.bonepeople.android.starvault.module.record.list

sealed interface RecordListUserAction {
    // 用户点击创建按钮
    object ClickCreate : RecordListUserAction
    // 用户点击列表项
    data class ClickItem(val recordId: String) : RecordListUserAction
}