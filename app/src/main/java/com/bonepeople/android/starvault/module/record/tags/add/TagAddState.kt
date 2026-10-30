package com.bonepeople.android.starvault.module.record.tags.add

import com.google.gson.annotations.SerializedName

data class TagAddState(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("nameError")
    val nameError: String = "",
    @SerializedName("submitting")
    val submitting: Boolean = false,
) {
    @Suppress("unused")
    object Preview {
        val Empty = TagAddState()
        val Normal = TagAddState(name = "家庭")
        val WithError = TagAddState(name = "开发", nameError = "标签已存在")
    }

    companion object {
        val Default = TagAddState()
    }
}