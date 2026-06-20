package com.bonepeople.android.starvault.module.record.detail

import com.google.gson.annotations.SerializedName

data class RecordDetailState(
    @SerializedName("loading")
    val loading: Boolean = false,
    @SerializedName("notFound")
    val notFound: Boolean = false,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("tags")
    val tags: List<String> = emptyList(),
    @SerializedName("fields")
    val fields: List<Field> = emptyList(),
) {
    data class Field(
        @SerializedName("id")
        val id: String = "",
        @SerializedName("name")
        val name: String = "",
        @SerializedName("currentValue")
        val currentValue: String = "",
        @SerializedName("historyExpanded")
        val historyExpanded: Boolean = false,
        @SerializedName("history")
        val history: List<HistoryItem> = emptyList(),
    ) {
        data class HistoryItem(
            @SerializedName("value")
            val value: String = "",
            @SerializedName("createTimestamp")
            val createTimestamp: Long = 0L,
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