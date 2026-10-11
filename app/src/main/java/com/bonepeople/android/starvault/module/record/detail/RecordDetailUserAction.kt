package com.bonepeople.android.starvault.module.record.detail

import com.bonepeople.android.starvault.global.data.VaultFieldInfo

sealed interface RecordDetailUserAction {
    // 用户点击标签区域
    object ClickTags : RecordDetailUserAction
    // 用户点击添加字段
    object ClickAddField : RecordDetailUserAction
    // 用户点击已有字段
    data class ClickField(val fieldId: String) : RecordDetailUserAction
    // 字段编辑页返回后更新页面字段
    data class FieldSaved(val field: VaultFieldInfo) : RecordDetailUserAction
    // 标签编辑页返回后回写标签
    data class TagsUpdated(val tags: List<String>) : RecordDetailUserAction
}