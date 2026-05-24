package com.bonepeople.android.starvault.global.data

data class VaultInfo(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val recordList: List<VaultRecordInfo> = emptyList(),
)