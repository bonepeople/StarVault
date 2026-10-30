package com.bonepeople.android.starvault.module.record.tags.add

sealed interface TagAddEffect {
    // 返回标签名称并关闭页面
    data class FinishAdded(val tag: String) : TagAddEffect
}