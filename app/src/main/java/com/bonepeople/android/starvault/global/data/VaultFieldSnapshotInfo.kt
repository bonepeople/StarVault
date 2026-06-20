package com.bonepeople.android.starvault.global.data

import com.google.gson.annotations.SerializedName

data class VaultFieldSnapshotInfo(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("value")
    val value: String = "",
    @SerializedName("createTimestamp")
    val createTimestamp: Long = 0L,
)