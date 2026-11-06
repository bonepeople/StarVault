package com.bonepeople.android.starvault.module.home

sealed interface HomeUserAction {
    // 用户点击创建按钮
    object ClickCreate : HomeUserAction
    // 用户点击打开按钮
    object ClickOpen : HomeUserAction
    // 用户点击测试按钮
    object ClickTest : HomeUserAction
}