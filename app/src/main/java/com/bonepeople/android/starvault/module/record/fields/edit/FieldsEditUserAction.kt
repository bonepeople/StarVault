package com.bonepeople.android.starvault.module.record.fields.edit

sealed interface FieldsEditUserAction {
    // 用户输入字段名称
    data class UpdateName(val name: String) : FieldsEditUserAction
    // 用户输入字段值
    data class UpdateValue(val value: String) : FieldsEditUserAction
    // 用户点击确认
    object ClickConfirm : FieldsEditUserAction
    // 用户长按完成删除字段
    object ClickDelete : FieldsEditUserAction
}