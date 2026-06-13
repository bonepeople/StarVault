package com.bonepeople.android.starvault.module.record.list

data class RecordListState(
    val loading: Boolean = false,
    val items: List<Item> = emptyList(),
) {
    data class Item(
        val id: String,
        val title: String,
        val tags: List<String>,
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