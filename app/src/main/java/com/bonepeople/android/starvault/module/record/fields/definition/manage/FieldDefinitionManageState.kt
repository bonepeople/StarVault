package com.bonepeople.android.starvault.module.record.fields.definition.manage

import com.google.gson.annotations.SerializedName

data class FieldDefinitionManageState(
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
        val Normal = FieldDefinitionManageState(
            items = listOf(
                Item(
                    id = "username",
                    name = "用户名",
                    description = "用于登录的账号名称",
                    usageCount = 1,
                ),
                Item(
                    id = "password",
                    name = "密码",
                    description = "用于登录的密码",
                    usageCount = 2,
                ),
                Item(
                    id = "recovery-email",
                    name = "恢复邮箱",
                ),
            ),
        )

        val Empty = FieldDefinitionManageState()
    }

    companion object {
        val Default = FieldDefinitionManageState()
    }
}