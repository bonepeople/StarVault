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

    @Suppress("unused")
    object Preview {
        val Normal = RecordDetailState(
            title = "GitHub",
            tags = listOf("开发", "工作"),
            fields = listOf(
                Field(
                    id = "1",
                    name = "用户名",
                    currentValue = "bonepeople",
                ),
                Field(
                    id = "2",
                    name = "密码",
                    currentValue = "P@ssw0rd_2026!",
                    history = listOf(
                        Field.HistoryItem("TmpP@ss2025", 1_758_355_200_000L),
                        Field.HistoryItem("OldP@ss2024", 1_717_228_800_000L),
                    ),
                    historyExpanded = true,
                ),
                Field(
                    id = "3",
                    name = "备注",
                    currentValue = "主账号，绑定 YubiKey",
                ),
            ),
        )

        val Loading = RecordDetailState(loading = true)

        val NotFound = RecordDetailState(notFound = true)
    }

    companion object {
        val Default = RecordDetailState()
    }
}
