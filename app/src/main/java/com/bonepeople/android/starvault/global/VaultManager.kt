package com.bonepeople.android.starvault.global

import com.bonepeople.android.starvault.global.data.VaultFieldDefinitionInfo
import com.bonepeople.android.starvault.global.data.VaultFieldInfo
import com.bonepeople.android.starvault.global.data.VaultFieldSnapshotInfo
import com.bonepeople.android.starvault.global.data.VaultInfo
import com.bonepeople.android.starvault.global.data.VaultRecordInfo
import com.bonepeople.android.starvault.global.data.VaultTagDefinitionInfo
import com.bonepeople.android.starvault.global.util.TagDefinitionUtil
import java.util.UUID

object VaultManager {
    var currentVault = VaultInfo()
        private set
    var recommendedTagDefinitionList: List<VaultTagDefinitionInfo> = emptyList()
        private set

    fun findRecordById(recordId: String): VaultRecordInfo? {
        return currentVault.recordList.find { it.id == recordId }
    }

    fun findFieldDefinitionById(definitionId: String): VaultFieldDefinitionInfo? {
        return currentVault.fieldDefinitionList.find { it.id == definitionId }
    }

    fun findTagDefinitionById(definitionId: String): VaultTagDefinitionInfo? {
        if (definitionId.isEmpty()) return null
        return currentVault.tagDefinitionList.find { it.id == definitionId }
    }

    fun findTagDefinitionByName(name: String): VaultTagDefinitionInfo? {
        val normalizedName = TagDefinitionUtil.normalizeName(name)
        if (normalizedName.isEmpty()) return null
        return currentVault.tagDefinitionList.find { TagDefinitionUtil.normalizeName(it.name) == normalizedName }
    }

    fun findTagDefinitionsByIds(definitionIds: List<String>): List<VaultTagDefinitionInfo> {
        val definitionMap = currentVault.tagDefinitionList.associateBy { it.id }
        return definitionIds.map { definitionMap.getValue(it) }
    }

    @Synchronized
    fun createTagDefinition(definition: VaultTagDefinitionInfo): VaultTagDefinitionInfo? {
        val name = definition.name.trim()
        if (definition.id.isEmpty() || TagDefinitionUtil.normalizeName(name).isEmpty()) return null
        if (findTagDefinitionById(definition.id) != null || findTagDefinitionByName(name) != null) return null
        val newDefinition = definition.copy(name = name)
        updateVault(currentVault.copy(tagDefinitionList = currentVault.tagDefinitionList + newDefinition))
        return newDefinition
    }

    fun createFieldDefinition(name: String, description: String): VaultFieldDefinitionInfo {
        val definition = VaultFieldDefinitionInfo(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            description = description.trim(),
        )
        updateVault(currentVault.copy(fieldDefinitionList = currentVault.fieldDefinitionList + definition))
        return definition
    }

    @Synchronized
    fun addRecord(record: VaultRecordInfo, tags: List<VaultTagDefinitionInfo>) {
        val (tagDefinitionList, resolvedTags) = resolveTagDefinitions(tags)
        val newRecord = record.copy(tagDefinitionIdList = resolvedTags.map { it.id })
        updateVault(
            currentVault.copy(
                tagDefinitionList = tagDefinitionList,
                recordList = currentVault.recordList + newRecord,
            ),
        )
    }

    @Synchronized
    fun updateRecordTags(recordId: String, tags: List<VaultTagDefinitionInfo>): List<VaultTagDefinitionInfo> {
        val (tagDefinitionList, resolvedTags) = resolveTagDefinitions(tags)
        updateVault(
            currentVault.copy(
                tagDefinitionList = tagDefinitionList,
                recordList = currentVault.recordList.map { record ->
                    if (record.id == recordId) record.copy(tagDefinitionIdList = resolvedTags.map { it.id }) else record
                },
            ),
        )
        return resolvedTags
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
        val tagDefinitionList = listOf(
            VaultTagDefinitionInfo(id = UUID.randomUUID().toString(), name = "开发"),
            VaultTagDefinitionInfo(id = UUID.randomUUID().toString(), name = "工作"),
            VaultTagDefinitionInfo(id = UUID.randomUUID().toString(), name = "家庭"),
            VaultTagDefinitionInfo(id = UUID.randomUUID().toString(), name = "网络"),
        )
        val tagDefinitionIdMap = tagDefinitionList.associate { it.name to it.id }
        updateVault(VaultInfo(
            id = UUID.randomUUID().toString(),
            name = "演示保险库",
            description = "用于 UI 与逻辑测试的示例数据",
            fieldDefinitionList = fieldDefinitionList,
            tagDefinitionList = tagDefinitionList,
            recordList = listOf(
                VaultRecordInfo(
                    id = UUID.randomUUID().toString(),
                    title = "GitHub",
                    tagDefinitionIdList = listOf(
                        tagDefinitionIdMap.getValue("开发"),
                        tagDefinitionIdMap.getValue("工作"),
                    ),
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
                    tagDefinitionIdList = listOf(
                        tagDefinitionIdMap.getValue("家庭"),
                        tagDefinitionIdMap.getValue("网络"),
                    ),
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
        val usageCountByDefinitionId = currentVault.recordList
            .flatMap { it.tagDefinitionIdList.distinct() }
            .groupingBy { it }
            .eachCount()
        recommendedTagDefinitionList = currentVault.tagDefinitionList.sortedWith(
            compareByDescending<VaultTagDefinitionInfo> { usageCountByDefinitionId[it.id] ?: 0 }
                .thenBy { TagDefinitionUtil.normalizeName(it.name) }
                .thenBy { it.id },
        )
    }

    private fun resolveTagDefinitions(tags: List<VaultTagDefinitionInfo>): Pair<List<VaultTagDefinitionInfo>, List<VaultTagDefinitionInfo>> {
        val tagDefinitionList = currentVault.tagDefinitionList.toMutableList()
        val resolvedTags = tags.map { tag ->
            val normalizedName = TagDefinitionUtil.normalizeName(tag.name)
            tagDefinitionList.find { it.id == tag.id }
                ?: tagDefinitionList.find { TagDefinitionUtil.normalizeName(it.name) == normalizedName }
                ?: tag.copy(name = tag.name.trim()).also { tagDefinitionList.add(it) }
        }.distinctBy { it.id }
        return tagDefinitionList to resolvedTags
    }
}