package com.bonepeople.android.starvault.module.record.fields.definition.manage

sealed interface FieldDefinitionManageUserAction {
    // 用户新建字段定义
    object ClickCreateDefinition : FieldDefinitionManageUserAction
    // 字段定义创建完成
    data class DefinitionCreated(val definitionId: String) : FieldDefinitionManageUserAction
}