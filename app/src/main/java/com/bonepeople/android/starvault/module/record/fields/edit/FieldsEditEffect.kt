package com.bonepeople.android.starvault.module.record.fields.edit

import com.bonepeople.android.starvault.global.data.VaultFieldInfo

sealed interface FieldsEditEffect {
    // 返回保存后的字段并关闭编辑页
    data class FinishSaved(val field: VaultFieldInfo) : FieldsEditEffect
}