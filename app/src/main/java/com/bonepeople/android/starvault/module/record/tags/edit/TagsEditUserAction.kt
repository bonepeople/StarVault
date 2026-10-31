package com.bonepeople.android.starvault.module.record.tags.edit

sealed interface TagsEditUserAction {
    // 用户点击「添加标签」
    object ClickAdd : TagsEditUserAction
    // 添加页返回的标签名称
    data class TagAdded(val tag: String) : TagsEditUserAction
    // 用户点击标签删除按钮
    data class ClickDelete(val tag: String) : TagsEditUserAction
    // 用户拖动标签调整顺序，将 from 位置的标签移动到 to 位置
    data class MoveTag(val from: Int, val to: Int) : TagsEditUserAction
    // 用户点击 Snackbar「撤回」
    object UndoDelete : TagsEditUserAction
    // 用户按返回键
    object ClickBack : TagsEditUserAction
    // 用户点击推荐标签
    data class ClickRecommendTag(val tag: String) : TagsEditUserAction
}