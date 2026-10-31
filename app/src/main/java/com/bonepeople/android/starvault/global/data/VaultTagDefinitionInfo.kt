package com.bonepeople.android.starvault.global.data

import com.google.gson.annotations.SerializedName

data class VaultTagDefinitionInfo(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("name")
    val name: String = "",
)