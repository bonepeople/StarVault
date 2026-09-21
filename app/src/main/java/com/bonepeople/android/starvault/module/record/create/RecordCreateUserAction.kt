package com.bonepeople.android.starvault.module.record.create

sealed interface RecordCreateUserAction {
    // 用户输入名称
    data class UpdateTitle(val title: String) : RecordCreateUserAction
    // 用户点击标签区域
    object ClickTags : RecordCreateUserAction
    // 标签编辑页返回后回写标签
    data class TagsUpdated(val tags: List<String>) : RecordCreateUserAction
    // 用户点击「下一步」
    object ClickNext : RecordCreateUserAction
}