package com.bonepeople.android.starvault.module.record.fields.edit

import com.bonepeople.android.starvault.global.data.VaultFieldInfo

sealed interface FieldsEditEffect {
    // 请求输入框获取初始焦点
    data class RequestFocus(val target: FocusTarget) : FieldsEditEffect
    // 返回保存后的字段并关闭编辑页
    data class FinishSaved(val field: VaultFieldInfo) : FieldsEditEffect
    // 返回删除的字段 ID 并关闭编辑页
    data class FinishDeleted(val fieldId: String) : FieldsEditEffect

    enum class FocusTarget {
        Name,
        Value,
    }
}