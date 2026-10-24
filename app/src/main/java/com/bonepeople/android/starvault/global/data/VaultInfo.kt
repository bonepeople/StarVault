package com.bonepeople.android.starvault.global.data

import com.google.gson.annotations.SerializedName

data class VaultInfo(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("fieldDefinitionList")
    val fieldDefinitionList: List<VaultFieldDefinitionInfo> = emptyList(),
    @SerializedName("recordList")
    val recordList: List<VaultRecordInfo> = emptyList(),
)