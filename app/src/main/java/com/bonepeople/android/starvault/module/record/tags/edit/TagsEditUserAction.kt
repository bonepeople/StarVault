package com.bonepeople.android.starvault.module.record.tags.edit

sealed interface TagsEditUserAction {
    // 用户点击「添加标签」
    object ClickOpenAddDialog : TagsEditUserAction
    // 用户点击弹窗中的「取消」
    object DismissAddDialog : TagsEditUserAction
    // 用户输入标签内容
    data class UpdateInput(val text: String) : TagsEditUserAction
    // 用户点击弹窗中的「添加」
    object ClickConfirmAdd : TagsEditUserAction
    // 用户点击标签删除按钮
    data class ClickDelete(val tag: String) : TagsEditUserAction
    // 用户点击 Snackbar「撤回」
    object UndoDelete : TagsEditUserAction
    // 用户按返回键
    object ClickBack : TagsEditUserAction
}