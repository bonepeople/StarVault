package com.bonepeople.android.starvault.module.home

data class HomeState(
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