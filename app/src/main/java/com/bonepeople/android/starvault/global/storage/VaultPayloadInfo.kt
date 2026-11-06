package com.bonepeople.android.starvault.global.storage

import com.bonepeople.android.starvault.global.data.VaultInfo
import com.google.gson.annotations.SerializedName

data class VaultPayloadInfo(
    @SerializedName("schemaVersion")
    val schemaVersion: Int = VaultFileProtocol.SCHEMA_VERSION,
    @SerializedName("revision")
    val revision: Long = VaultFileProtocol.INITIAL_REVISION,
    @SerializedName("vault")
    val vault: VaultInfo = VaultInfo(),
)