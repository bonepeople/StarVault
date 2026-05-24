package com.bonepeople.android.starvault.global.data

data class VaultRecordInfo(
    val id: String = "",
    val title: String = "",
    val fieldList: List<VaultFieldInfo> = emptyList(),
    val tagList: List<String> = emptyList(),
)