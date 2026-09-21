package com.bonepeople.android.starvault.module.record.detail

sealed interface RecordDetailUserAction {
    // 用户点击标签区域
    object ClickTags : RecordDetailUserAction
    // 标签编辑页返回后回写标签
    data class TagsUpdated(val tags: List<String>) : RecordDetailUserAction
    // 用户点击展开/收起字段历史
    data class ToggleFieldHistory(val fieldId: String) : RecordDetailUserAction
}