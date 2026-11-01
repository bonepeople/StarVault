package com.bonepeople.android.starvault.module.record.list

import com.bonepeople.android.starvault.global.data.VaultTagDefinitionInfo
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
        val tags: List<VaultTagDefinitionInfo> = emptyList(),
    )

    @Suppress("unused")
    object Preview {
        val Normal = RecordListState(
            items = listOf(
                Item(
                    id = "1",
                    title = "GitHub",
                    tags = listOf(
                        VaultTagDefinitionInfo(id = "tag-dev", name = "开发"),
                        VaultTagDefinitionInfo(id = "tag-work", name = "工作"),
                    ),
                ),
                Item(id = "2", title = "Wi‑Fi 密码", tags = listOf(VaultTagDefinitionInfo(id = "tag-long", name = "这是一个很长很长很长很长很长很长很长很长的标签名称"))),
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