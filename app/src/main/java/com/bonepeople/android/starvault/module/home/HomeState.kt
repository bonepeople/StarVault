package com.bonepeople.android.starvault.module.home

import com.google.gson.annotations.SerializedName

data class HomeState(
    @SerializedName("loading")
    val loading: Boolean = false,
) {
    @Suppress("unused")
    object Preview {
        val Normal = HomeState()

        val Loading = HomeState(loading = true)
    }

    companion object {
        val Default = HomeState()
    }
}