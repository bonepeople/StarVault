package com.bonepeople.android.starvault.module.record.tags.edit

sealed interface TagsEditEffect {
    data class FinishWithResult(val tags: List<String>) : TagsEditEffect
}