package com.bonepeople.android.starvault.global.data

/**
 * @param snapshotList 按时间倒序排列，最新快照在首位；新增状态时插入到第一个位置
 */
data class VaultFieldInfo(
    val id: String = "",
    val snapshotList: List<VaultFieldSnapshotInfo> = emptyList(),
)