package com.bonepeople.android.starvault.global.util

import com.bonepeople.android.widget.util.AppData

object DataUtil {
    val appStart: AppData by lazy { AppData.create("appStart") }
    val main: AppData by lazy { AppData.create("main") }

    object Key {
        object AppStart {
            const val HAS_VIEWED_GUIDE = "HAS_VIEWED_GUIDE"
        }
    }
}