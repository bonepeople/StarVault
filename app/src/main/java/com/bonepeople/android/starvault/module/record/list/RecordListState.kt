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
}