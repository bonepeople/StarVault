package com.bonepeople.android.starvault.module.record.tags.edit

import com.bonepeople.android.starvault.global.data.VaultTagDefinitionInfo

sealed interface TagsEditEffect {
    // 打开添加标签页，并带上当前记录已有标签
    data class OpenTagAdd(val existingTags: List<VaultTagDefinitionInfo>) : TagsEditEffect
    // 关闭页面并返回标签模型
    data class FinishWithResult(val tags: List<VaultTagDefinitionInfo>) : TagsEditEffect
}