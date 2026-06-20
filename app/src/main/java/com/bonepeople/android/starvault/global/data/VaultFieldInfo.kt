package com.bonepeople.android.starvault.global.data

import com.google.gson.annotations.SerializedName

/**
 * @param snapshotList 按时间倒序排列，最新快照在首位；新增状态时插入到第一个位置
 */
data class VaultFieldInfo(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("snapshotList")
    val snapshotList: List<VaultFieldSnapshotInfo> = emptyList(),
)