package com.bonepeople.android.starvault.module.record.tags.add

sealed interface TagAddUserAction {
    // 用户输入标签名称
    data class UpdateName(val name: String) : TagAddUserAction
    // 用户点击确认添加
    object ClickConfirm : TagAddUserAction
}