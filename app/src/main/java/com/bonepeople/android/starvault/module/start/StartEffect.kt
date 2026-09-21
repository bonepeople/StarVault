package com.bonepeople.android.starvault.module.start

sealed interface StartEffect {
    // 去引导页
    object NavigateToGuide : StartEffect
    // 去首页
    object NavigateToHome : StartEffect
}