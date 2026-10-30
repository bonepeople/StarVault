package com.bonepeople.android.starvault.module.record.tags.edit

import com.google.gson.annotations.SerializedName

data class TagsEditState(
    @SerializedName("tags")
    val tags: List<String> = emptyList(),
    @SerializedName("pendingUndo")
    val pendingUndo: PendingUndoInfo? = null,
    @SerializedName("recommendedTags")
    val recommendedTags: List<String> = emptyList(),
) {
    @Suppress("unused")
    object Preview {
        val Normal = TagsEditState(
            tags = listOf("开发", "工作"),
            recommendedTags = listOf("家庭", "网络"),
        )

        val Empty = TagsEditState()
    }

    companion object {
        val Default = TagsEditState()
    }
}