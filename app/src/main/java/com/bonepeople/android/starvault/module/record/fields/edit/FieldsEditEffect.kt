package com.bonepeople.android.starvault.module.record.fields.edit

sealed interface FieldsEditEffect {
    // 关闭字段编辑页
    object Finish : FieldsEditEffect
}