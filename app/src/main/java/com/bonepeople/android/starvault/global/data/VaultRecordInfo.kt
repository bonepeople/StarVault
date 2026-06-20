package com.bonepeople.android.starvault.global.data

import com.google.gson.annotations.SerializedName

data class VaultRecordInfo(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("title")
    val title: String = "",
    @SerializedName("fieldList")
    val fieldList: List<VaultFieldInfo> = emptyList(),
    @SerializedName("tagList")
    val tagList: List<String> = emptyList(),
)