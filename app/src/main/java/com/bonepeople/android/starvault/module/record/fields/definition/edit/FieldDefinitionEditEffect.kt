package com.bonepeople.android.starvault.module.record.fields.definition.edit

sealed interface FieldDefinitionEditEffect {
    // 返回新建的字段定义 ID 并关闭页面
    data class FinishCreated(val definitionId: String) : FieldDefinitionEditEffect
}