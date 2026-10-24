package com.bonepeople.android.starvault.global.data

import com.google.gson.annotations.SerializedName

/**
 * @param definitionId 字段定义 ID
 * @param snapshotList 字段值按时间倒序排列，最新快照在首位；值变化时插入到第一个位置
 */
data class VaultFieldInfo(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("definitionId")
    val definitionId: String = "",
    @SerializedName("snapshotList")
    val snapshotList: List<VaultFieldSnapshotInfo> = emptyList(),
)