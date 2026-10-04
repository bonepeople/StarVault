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
) {
    @Suppress("unused")
    object Preview {
        val Empty = FieldsEditState()

        val Normal = FieldsEditState(id = "field-id", name = "用户名", value = "bonepeople")

        val WithError = FieldsEditState(nameError = "请输入字段名称", valueError = "请输入字段值")
    }

    companion object {
        val Default = FieldsEditState()
    }
}