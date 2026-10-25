package com.bonepeople.android.starvault.module.record.fields.select

sealed interface FieldsSelectUserAction {
    // 用户输入搜索内容
    data class UpdateQuery(val query: String) : FieldsSelectUserAction
}