package com.bonepeople.android.starvault.module.record.fields.definition.edit

import com.google.gson.annotations.SerializedName

data class FieldDefinitionEditState(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("nameError")
    val nameError: String = "",
    @SerializedName("saving")
    val saving: Boolean = false,
) {
    @Suppress("unused")
    object Preview {
        val Empty = FieldDefinitionEditState()
        val Normal = FieldDefinitionEditState(
            name = "账号",
            description = "用于登录的账号名称",
        )
        val WithError = Normal.copy(nameError = "信息类型名称已存在")
    }

    companion object {
        val Default = FieldDefinitionEditState()
    }
}