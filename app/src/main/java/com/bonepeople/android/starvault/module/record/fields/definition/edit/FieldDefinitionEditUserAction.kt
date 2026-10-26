package com.bonepeople.android.starvault.module.record.fields.definition.edit

sealed interface FieldDefinitionEditUserAction {
    // 用户输入字段定义名称
    data class UpdateName(val name: String) : FieldDefinitionEditUserAction
    // 用户输入字段定义说明
    data class UpdateDescription(val description: String) : FieldDefinitionEditUserAction
    // 用户点击确认创建
    object ClickConfirm : FieldDefinitionEditUserAction
}