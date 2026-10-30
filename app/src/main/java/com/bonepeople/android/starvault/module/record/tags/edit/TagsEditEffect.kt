package com.bonepeople.android.starvault.module.record.tags.edit

sealed interface TagsEditEffect {
    // 打开添加标签页，并带上当前记录已有标签
    data class OpenTagAdd(val existingTags: List<String>) : TagsEditEffect
    // 关闭页面并返回标签数据
    data class FinishWithResult(val tags: List<String>) : TagsEditEffect
}