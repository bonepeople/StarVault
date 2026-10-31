package com.bonepeople.android.starvault.module.record.tags.add

import com.bonepeople.android.starvault.global.data.VaultTagDefinitionInfo

sealed interface TagAddEffect {
    // 返回标签模型并关闭页面
    data class FinishAdded(val tag: VaultTagDefinitionInfo) : TagAddEffect
}