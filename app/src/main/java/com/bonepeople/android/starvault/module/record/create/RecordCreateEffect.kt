package com.bonepeople.android.starvault.module.record.create

import com.bonepeople.android.starvault.global.data.VaultTagDefinitionInfo

sealed interface RecordCreateEffect {
    // 打开详情页
    data class OpenDetail(val recordId: String) : RecordCreateEffect
    // 打开标签编辑页
    data class OpenTagsEdit(val tags: List<VaultTagDefinitionInfo>) : RecordCreateEffect
}