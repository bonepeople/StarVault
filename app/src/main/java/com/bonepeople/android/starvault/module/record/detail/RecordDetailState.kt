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
    )

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