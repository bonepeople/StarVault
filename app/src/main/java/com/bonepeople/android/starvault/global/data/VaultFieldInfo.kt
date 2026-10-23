package com.bonepeople.android.starvault.global.data

import com.google.gson.annotations.SerializedName

/**
 * @param name 字段当前名称
 * @param snapshotList 字段值按时间倒序排列，最新快照在首位；值变化时插入到第一个位置
 */
data class VaultFieldInfo(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("snapshotList")
    val snapshotList: List<VaultFieldSnapshotInfo> = emptyList(),
)