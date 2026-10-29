package com.bonepeople.android.starvault.module.record.fields.definition.manage

sealed interface FieldDefinitionManageEffect {
    // 打开字段定义创建页
    object OpenDefinitionCreate : FieldDefinitionManageEffect
}