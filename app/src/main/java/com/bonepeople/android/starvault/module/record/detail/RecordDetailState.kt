package com.bonepeople.android.starvault.module.record.detail

data class RecordDetailState(
    val loading: Boolean = false,
    val notFound: Boolean = false,
    val title: String = "",
    val tags: List<String> = emptyList(),
    val fields: List<Field> = emptyList(),
) {
    data class Field(
        val id: String,
        val name: String,
        val currentValue: String,
        val historyExpanded: Boolean = false,
        val history: List<HistoryItem> = emptyList(),
    ) {
        data class HistoryItem(
            val value: String,
            val createTimestamp: Long,
        )
    }
}
