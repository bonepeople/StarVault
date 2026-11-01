package com.bonepeople.android.starvault.module.record.tags.edit

import com.bonepeople.android.starvault.global.data.VaultTagDefinitionInfo
import com.google.gson.annotations.SerializedName

data class PendingUndoInfo(
    @SerializedName("tag")
    val tag: VaultTagDefinitionInfo = VaultTagDefinitionInfo(),
    @SerializedName("index")
    val index: Int = 0,
)