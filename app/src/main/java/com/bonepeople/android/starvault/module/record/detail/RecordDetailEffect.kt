package com.bonepeople.android.starvault.module.record.detail

import com.bonepeople.android.starvault.global.data.VaultFieldInfo

sealed interface RecordDetailEffect {
    // 打开标签编辑页
    data class OpenTagsEdit(val tags: List<String>) : RecordDetailEffect
    // 打开字段编辑页
    data class OpenFieldsEdit(val field: VaultFieldInfo) : RecordDetailEffect
    // 删除记录后关闭详情页
    object FinishDeleted : RecordDetailEffect
}