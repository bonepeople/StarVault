package com.bonepeople.android.starvault.module.home

sealed interface HomeUserAction {
    object ClickCreate : HomeUserAction
    object ClickOpen : HomeUserAction
}