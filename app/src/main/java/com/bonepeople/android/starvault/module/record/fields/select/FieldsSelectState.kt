package com.bonepeople.android.starvault.module.record.fields.select

import com.google.gson.annotations.SerializedName

data class FieldsSelectState(
    @SerializedName("query")
    val query: String = "",
    @SerializedName("items")
    val items: List<Item> = emptyList(),
) {
    data class Item(
        @SerializedName("id")
        val id: String = "",
        @SerializedName("name")
        val name: String = "",
        @SerializedName("description")
        val description: String = "",
        @SerializedName("usageCount")
        val usageCount: Int = 0,
    )

    @Suppress("unused")
    object Preview {
        val Normal = FieldsSelectState(
            items = listOf(
                Item(
                    id = "password",
                    name = "密码",
                    description = "用于登录的密码",
                    usageCount = 2,
                ),
                Item(
                    id = "username",
                    name = "用户名",
                    description = "用于登录的账号名称",
                    usageCount = 1,
                ),
                Item(
                    id = "note",
                    name = "备注",
                ),
            ),
        )

        val Empty = FieldsSelectState()

        val NoResults = FieldsSelectState(query = "令牌")
    }

    companion object {
        val Default = FieldsSelectState()
    }
}