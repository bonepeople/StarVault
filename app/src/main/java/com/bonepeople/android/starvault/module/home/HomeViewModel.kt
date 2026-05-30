package com.bonepeople.android.starvault.module.home

import androidx.lifecycle.ViewModel
import com.bonepeople.android.starvault.global.util.LogUtil
import com.bonepeople.android.widget.util.AppToast

class HomeViewModel : ViewModel() {
    fun createVault() {
        LogUtil.test.info("点击创建")
        AppToast.show("施工中")
    }

    fun openVault() {
        LogUtil.test.info("点击打开")
        AppToast.show("施工中")
    }
}
