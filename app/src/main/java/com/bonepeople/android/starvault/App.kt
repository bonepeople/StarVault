package com.bonepeople.android.starvault

import android.app.Application
import com.bonepeople.android.base.activity.StandardActivity
import com.gyf.immersionbar.ktx.immersionBar

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        StandardActivity.statusBar = {
            immersionBar {
                transparentBar()
                statusBarDarkFont(true)
                navigationBarDarkIcon(true)
            }
        }
    }
}