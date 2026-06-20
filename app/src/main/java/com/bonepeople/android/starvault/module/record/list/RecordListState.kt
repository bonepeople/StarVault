package com.bonepeople.android.starvault.module.record.list

import com.google.gson.annotations.SerializedName

data class RecordListState(
    @SerializedName("loading")
    val loading: Boolean = false,
    @SerializedName("items")
    val items: List<Item> = emptyList(),
) {
    data class Item(
        @SerializedName("id")
        val id: String = "",
        @SerializedName("title")
        val title: String = "",
        @SerializedName("tags")
        val tags: List<String> = emptyList(),
    )

    @Suppress("unused")
    object Preview {
        val Normal = RecordListState(
            items = listOf(
                Item(id = "1", title = "GitHub", tags = listOf("开发", "工作")),
                Item(id = "2", title = "Wi‑Fi 密码", tags = listOf("Home")),
                Item(id = "3", title = "无标签示例", tags = emptyList()),
            ),
        )

        val Loading = RecordListState(loading = true)

        val Empty = RecordListState()
    }

    companion object {
        val Default = RecordListState()
    }
}