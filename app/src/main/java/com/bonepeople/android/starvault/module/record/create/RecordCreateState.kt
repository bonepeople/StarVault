package com.bonepeople.android.starvault.module.record.create

import com.google.gson.annotations.SerializedName

data class RecordCreateState(
    @SerializedName("title")
    val title: String = "",
    @SerializedName("tags")
    val tags: List<String> = emptyList(),
    @SerializedName("titleError")
    val titleError: String = "",
    @SerializedName("saving")
    val saving: Boolean = false,
) {
    @Suppress("unused")
    object Preview {
        val Normal = RecordCreateState(
            title = "GitHub",
            tags = listOf("开发", "工作"),
        )

        val Empty = RecordCreateState()

        val TitleError = RecordCreateState(
            title = "   ",
            titleError = "请输入名称",
        )
    }

    companion object {
        val Default = RecordCreateState()
    }
}