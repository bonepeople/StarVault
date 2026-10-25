package com.bonepeople.android.starvault.module.record.fields.select

sealed interface FieldsSelectUserAction {
    // 用户输入搜索内容
    data class UpdateQuery(val query: String) : FieldsSelectUserAction
    // 用户选择字段定义
    data class ClickItem(val definitionId: String) : FieldsSelectUserAction
}