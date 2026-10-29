package com.bonepeople.android.starvault.module.record.fields.select

sealed interface FieldsSelectEffect {
    // 打开字段定义创建页
    object OpenDefinitionCreate : FieldsSelectEffect
    // 打开字段定义管理页
    object OpenDefinitionManage : FieldsSelectEffect
    // 返回选中的字段定义 ID 并关闭选择页
    data class FinishSelected(val definitionId: String) : FieldsSelectEffect
}