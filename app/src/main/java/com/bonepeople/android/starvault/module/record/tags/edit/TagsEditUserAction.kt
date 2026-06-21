package com.bonepeople.android.starvault.module.record.tags.edit

sealed interface TagsEditUserAction {
    object ClickOpenAddDialog : TagsEditUserAction
    object DismissAddDialog : TagsEditUserAction
    data class UpdateInput(val text: String) : TagsEditUserAction
    object ClickConfirmAdd : TagsEditUserAction
    data class ClickDelete(val tag: String) : TagsEditUserAction
    object UndoDelete : TagsEditUserAction
    object ClickBack : TagsEditUserAction
}