package com.bonepeople.android.starvault.module.record.tags.edit

import com.google.gson.annotations.SerializedName

data class TagsEditState(
    @SerializedName("tags")
    val tags: List<String> = emptyList(),
    @SerializedName("addDialogVisible")
    val addDialogVisible: Boolean = false,
    @SerializedName("inputText")
    val inputText: String = "",
    @SerializedName("inputError")
    val inputError: String = "",
    @SerializedName("pendingUndo")
    val pendingUndo: PendingUndoInfo? = null,
) {
    @Suppress("unused")
    object Preview {
        val Normal = TagsEditState(
            tags = listOf("开发", "工作"),
        )

        val Empty = TagsEditState()

        val DialogOpen = TagsEditState(
            tags = listOf("开发"),
            addDialogVisible = true,
            inputText = "新标签",
        )

        val WithError = TagsEditState(
            tags = listOf("开发"),
            addDialogVisible = true,
            inputText = "开发",
            inputError = "标签已存在",
        )
    }

    companion object {
        val Default = TagsEditState()
    }
}