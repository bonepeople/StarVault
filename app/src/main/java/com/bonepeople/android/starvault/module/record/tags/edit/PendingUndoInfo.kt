package com.bonepeople.android.starvault.module.record.tags.edit

import com.google.gson.annotations.SerializedName

data class PendingUndoInfo(
    @SerializedName("tag")
    val tag: String,
    @SerializedName("index")
    val index: Int,
)