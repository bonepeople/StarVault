package com.bonepeople.android.starvault.module.record.tags.edit

sealed interface TagsEditEffect {
    // 关闭页面并返回标签数据
    data class FinishWithResult(val tags: List<String>) : TagsEditEffect
}