package com.bonepeople.android.starvault.module.record.detail

sealed interface RecordDetailEffect {
    data class OpenTagsEdit(val tags: List<String>) : RecordDetailEffect
}