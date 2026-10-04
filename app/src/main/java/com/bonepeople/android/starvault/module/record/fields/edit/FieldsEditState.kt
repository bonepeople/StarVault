package com.bonepeople.android.starvault.module.record.fields.edit

import com.google.gson.annotations.SerializedName

data class FieldsEditState(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("value")
    val value: String = "",
    @SerializedName("nameError")
    val nameError: String = "",
    @SerializedName("valueError")
    val valueError: String = "",
    @SerializedName("history")
    val history: List<HistoryItem> = emptyList(),
) {
    data class HistoryItem(
        @SerializedName("value")
        val value: String = "",
        @SerializedName("createTimestamp")
        val createTimestamp: Long = 0L,
    )

    @Suppress("unused")
    object Preview {
        val Empty = FieldsEditState()

        val Normal = FieldsEditState(
            id = "field-id",
            name = "用户名",
            value = "bonepeople",
            history = listOf(
                HistoryItem(value = "bonepeople_2025", createTimestamp = 1_758_355_200_000L),
                HistoryItem(value = "bonepeople_2024", createTimestamp = 1_717_228_800_000L),
            ),
        )

        val WithError = FieldsEditState(nameError = "请输入字段名称", valueError = "请输入字段值")
    }

    companion object {
        val Default = FieldsEditState()
    }
}