package com.bonepeople.android.starvault.module.start

sealed interface StartEffect {
    object NavigateToGuide : StartEffect
    object NavigateToHome : StartEffect
}