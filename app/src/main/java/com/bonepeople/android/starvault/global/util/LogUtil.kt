package com.bonepeople.android.starvault.global.util

import com.bonepeople.android.widget.ApplicationHolder
import com.bonepeople.android.widget.util.AppLog

object LogUtil {
    val ENABLE: Boolean = ApplicationHolder.debug
    val app: AppLog = AppLog.tag("RLog.StarVault.app")
    val test: AppLog = AppLog.tag("RLog.StarVault.test")

    init {
        AppLog.enable = ENABLE
    }
}