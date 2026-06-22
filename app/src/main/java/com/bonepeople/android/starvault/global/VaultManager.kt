package com.bonepeople.android.starvault.global

import com.bonepeople.android.starvault.global.data.VaultFieldInfo
import com.bonepeople.android.starvault.global.data.VaultFieldSnapshotInfo
import com.bonepeople.android.starvault.global.data.VaultInfo
import com.bonepeople.android.starvault.global.data.VaultRecordInfo
import java.util.UUID

object VaultManager {
    var currentVault = VaultInfo()
        private set
    var recommendedTagList: List<String> = emptyList()
        private set

    fun findRecordById(recordId: String): VaultRecordInfo? {
        return currentVault.recordList.find { it.id == recordId }
    }

    fun addRecord(record: VaultRecordInfo) {
        updateVault(currentVault.copy(recordList = currentVault.recordList + record))
    }

    fun updateRecordTags(recordId: String, tagList: List<String>) {
        updateVault(
            currentVault.copy(
                recordList = currentVault.recordList.map { record ->
                    if (record.id == recordId) record.copy(tagList = tagList) else record
                },
            ),
        )
    }

    fun generateFakeVault() {
        updateVault(VaultInfo(
            id = UUID.randomUUID().toString(),
            name = "演示保险库",
            description = "用于 UI 与逻辑测试的示例数据",
            recordList = listOf(
                VaultRecordInfo(
                    id = UUID.randomUUID().toString(),
                    title = "GitHub",
                    tagList = listOf("开发", "工作"),
                    fieldList = listOf(
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(
                                    id = UUID.randomUUID().toString(),
                                    name = "用户名",
                                    value = "bonepeople",
                                    createTimestamp = 1_767_168_000_000L, // 2026-01-01
                                ),
                            ),
                        ),
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(
                                    id = UUID.randomUUID().toString(),
                                    name = "邮箱",
                                    value = "dev@example.com",
                                    createTimestamp = 1_767_168_000_000L,
                                ),
                            ),
                        ),
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(
                                    id = UUID.randomUUID().toString(),
                                    name = "密码",
                                    value = "P@ssw0rd_2026!",
                                    createTimestamp = 1_767_168_000_000L, // 2026-01-01
                                ),
                                VaultFieldSnapshotInfo(
                                    id = UUID.randomUUID().toString(),
                                    name = "密码",
                                    value = "TmpP@ss2025",
                                    createTimestamp = 1_758_355_200_000L, // 2025-09-20
                                ),
                                VaultFieldSnapshotInfo(
                                    id = UUID.randomUUID().toString(),
                                    name = "密码",
                                    value = "MidP@ss2025",
                                    createTimestamp = 1_736_947_200_000L, // 2025-01-15
                                ),
                                VaultFieldSnapshotInfo(
                                    id = UUID.randomUUID().toString(),
                                    name = "密码",
                                    value = "OldP@ss2024",
                                    createTimestamp = 1_717_228_800_000L, // 2024-06-01
                                ),
                            ),
                        ),
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(
                                    id = UUID.randomUUID().toString(),
                                    name = "2FA 密钥",
                                    value = "JBSWY3DPEHPK3PXP",
                                    createTimestamp = 1_767_168_000_000L,
                                ),
                            ),
                        ),
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(
                                    id = UUID.randomUUID().toString(),
                                    name = "备注",
                                    value = "主账号，绑定 YubiKey",
                                    createTimestamp = 1_767_168_000_000L,
                                ),
                            ),
                        ),
                    ),
                ),
                VaultRecordInfo(
                    id = UUID.randomUUID().toString(),
                    title = "家庭 Wi‑Fi",
                    tagList = listOf("家庭", "网络"),
                    fieldList = listOf(
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(
                                    id = UUID.randomUUID().toString(),
                                    name = "SSID",
                                    value = "StarHome_5G",
                                    createTimestamp = 1_767_168_000_000L,
                                ),
                            ),
                        ),
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(
                                    id = UUID.randomUUID().toString(),
                                    name = "密码",
                                    value = "wifi-secret-88",
                                    createTimestamp = 1_767_168_000_000L, // 2026-01-01
                                ),
                                VaultFieldSnapshotInfo(
                                    id = UUID.randomUUID().toString(),
                                    name = "密码",
                                    value = "router-2024",
                                    createTimestamp = 1_723_267_200_000L, // 2024-08-10
                                ),
                                VaultFieldSnapshotInfo(
                                    id = UUID.randomUUID().toString(),
                                    name = "密码",
                                    value = "old-wifi-2023",
                                    createTimestamp = 1_701_398_400_000L, // 2023-12-01
                                ),
                            ),
                        ),
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(
                                    id = UUID.randomUUID().toString(),
                                    name = "路由器地址",
                                    value = "192.168.1.1",
                                    createTimestamp = 1_767_168_000_000L,
                                ),
                            ),
                        ),
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(
                                    id = UUID.randomUUID().toString(),
                                    name = "管理 PIN",
                                    value = "12345678",
                                    createTimestamp = 1_767_168_000_000L,
                                ),
                            ),
                        ),
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(
                                    id = UUID.randomUUID().toString(),
                                    name = "运营商账号",
                                    value = "fiber@isp.example",
                                    createTimestamp = 1_767_168_000_000L,
                                ),
                            ),
                        ),
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(
                                    id = UUID.randomUUID().toString(),
                                    name = "客服电话",
                                    value = "10086",
                                    createTimestamp = 1_767_168_000_000L,
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        ))
    }

    private fun updateVault(vault: VaultInfo) {
        currentVault = vault
        refreshRecommendedTags()
    }

    private fun refreshRecommendedTags() {
        recommendedTagList = currentVault.recordList
            .flatMap { it.tagList }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { it.key }
    }
}