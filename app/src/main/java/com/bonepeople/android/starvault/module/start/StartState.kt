package com.bonepeople.android.starvault.module.start

import com.google.gson.annotations.SerializedName

data class StartState(
    @SerializedName("error")
    val error: Boolean = false,
    @SerializedName("appVersion")
    val appVersion: String = "",
) {
    @Suppress("unused")
    object Preview {
        val Normal = StartState(appVersion = "0.1.0")

        val Error = StartState(error = true, appVersion = "0.1.0")
    }

    companion object {
        val Default = StartState()
    }
}