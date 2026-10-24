package com.bonepeople.android.starvault.global

import com.bonepeople.android.starvault.global.data.VaultFieldDefinitionInfo
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

    fun findFieldDefinitionById(definitionId: String): VaultFieldDefinitionInfo? {
        return currentVault.fieldDefinitionList.find { it.id == definitionId }
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

    fun updateRecordFields(recordId: String, fieldList: List<VaultFieldInfo>) {
        updateVault(
            currentVault.copy(
                recordList = currentVault.recordList.map { record ->
                    if (record.id == recordId) record.copy(fieldList = fieldList) else record
                },
            ),
        )
    }

    fun deleteRecord(recordId: String) {
        if (findRecordById(recordId) == null) return
        updateVault(currentVault.copy(recordList = currentVault.recordList.filterNot { it.id == recordId }))
    }

    fun deleteRecordField(recordId: String, fieldId: String) {
        val record = findRecordById(recordId) ?: return
        if (record.fieldList.none { it.id == fieldId }) return
        updateVault(
            currentVault.copy(
                recordList = currentVault.recordList.map { item ->
                    if (item.id == recordId) {
                        item.copy(fieldList = item.fieldList.filterNot { it.id == fieldId })
                    } else {
                        item
                    }
                },
            ),
        )
    }

    fun generateFakeVault() {
        val fieldDefinitionList = listOf(
            VaultFieldDefinitionInfo(
                id = UUID.randomUUID().toString(),
                name = "用户名",
                description = "用于登录的账号名称",
            ),
            VaultFieldDefinitionInfo(
                id = UUID.randomUUID().toString(),
                name = "邮箱",
                description = "用于接收验证邮件",
            ),
            VaultFieldDefinitionInfo(
                id = UUID.randomUUID().toString(),
                name = "密码",
                description = "用于登录的密码",
            ),
            VaultFieldDefinitionInfo(
                id = UUID.randomUUID().toString(),
                name = "2FA 密钥",
                description = "用于双重认证的一次性密钥",
            ),
            VaultFieldDefinitionInfo(
                id = UUID.randomUUID().toString(),
                name = "备注",
            ),
            VaultFieldDefinitionInfo(
                id = UUID.randomUUID().toString(),
                name = "SSID",
            ),
            VaultFieldDefinitionInfo(
                id = UUID.randomUUID().toString(),
                name = "路由器地址",
            ),
            VaultFieldDefinitionInfo(
                id = UUID.randomUUID().toString(),
                name = "管理 PIN",
            ),
            VaultFieldDefinitionInfo(
                id = UUID.randomUUID().toString(),
                name = "运营商账号",
            ),
            VaultFieldDefinitionInfo(
                id = UUID.randomUUID().toString(),
                name = "客服电话",
            ),
        )
        val fieldDefinitionIdMap = fieldDefinitionList.associate { it.name to it.id }
        updateVault(VaultInfo(
            id = UUID.randomUUID().toString(),
            name = "演示保险库",
            description = "用于 UI 与逻辑测试的示例数据",
            fieldDefinitionList = fieldDefinitionList,
            recordList = listOf(
                VaultRecordInfo(
                    id = UUID.randomUUID().toString(),
                    title = "GitHub",
                    tagList = listOf("开发", "工作"),
                    fieldList = listOf(
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            definitionId = fieldDefinitionIdMap.getValue("用户名"),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(value = "bonepeople", createTimestamp = 1_767_168_000_000L), // 2026-01-01
                            ),
                        ),
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            definitionId = fieldDefinitionIdMap.getValue("邮箱"),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(value = "dev@example.com", createTimestamp = 1_767_168_000_000L),
                            ),
                        ),
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            definitionId = fieldDefinitionIdMap.getValue("密码"),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(value = "P@ssw0rd_2026!", createTimestamp = 1_767_168_000_000L), // 2026-01-01
                                VaultFieldSnapshotInfo(value = "TmpP@ss2025", createTimestamp = 1_758_355_200_000L), // 2025-09-20
                                VaultFieldSnapshotInfo(value = "MidP@ss2025", createTimestamp = 1_736_947_200_000L), // 2025-01-15
                                VaultFieldSnapshotInfo(value = "OldP@ss2024", createTimestamp = 1_717_228_800_000L), // 2024-06-01
                            ),
                        ),
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            definitionId = fieldDefinitionIdMap.getValue("2FA 密钥"),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(value = "JBSWY3DPEHPK3PXP", createTimestamp = 1_767_168_000_000L),
                            ),
                        ),
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            definitionId = fieldDefinitionIdMap.getValue("备注"),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(value = "主账号，绑定 YubiKey", createTimestamp = 1_767_168_000_000L),
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
                            definitionId = fieldDefinitionIdMap.getValue("SSID"),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(value = "StarHome_5G", createTimestamp = 1_767_168_000_000L),
                            ),
                        ),
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            definitionId = fieldDefinitionIdMap.getValue("密码"),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(value = "wifi-secret-88", createTimestamp = 1_767_168_000_000L), // 2026-01-01
                                VaultFieldSnapshotInfo(value = "router-2024", createTimestamp = 1_723_267_200_000L), // 2024-08-10
                                VaultFieldSnapshotInfo(value = "old-wifi-2023", createTimestamp = 1_701_398_400_000L), // 2023-12-01
                            ),
                        ),
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            definitionId = fieldDefinitionIdMap.getValue("路由器地址"),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(value = "192.168.1.1", createTimestamp = 1_767_168_000_000L),
                            ),
                        ),
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            definitionId = fieldDefinitionIdMap.getValue("管理 PIN"),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(value = "12345678", createTimestamp = 1_767_168_000_000L),
                            ),
                        ),
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            definitionId = fieldDefinitionIdMap.getValue("运营商账号"),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(value = "fiber@isp.example", createTimestamp = 1_767_168_000_000L),
                            ),
                        ),
                        VaultFieldInfo(
                            id = UUID.randomUUID().toString(),
                            definitionId = fieldDefinitionIdMap.getValue("客服电话"),
                            snapshotList = listOf(
                                VaultFieldSnapshotInfo(value = "10086", createTimestamp = 1_767_168_000_000L),
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