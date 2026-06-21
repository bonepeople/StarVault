package com.bonepeople.android.starvault.module.record.detail

sealed interface RecordDetailEffect {
    // 打开标签编辑页
    data class OpenTagsEdit(val tags: List<String>) : RecordDetailEffect
}