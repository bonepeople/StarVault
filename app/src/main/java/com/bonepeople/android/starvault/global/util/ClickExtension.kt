package com.bonepeople.android.starvault.global.util

import android.os.SystemClock
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role

object ClickExtension {
    private const val DEFAULT_SINGLE_CLICK_INTERVAL = 800L
    private const val NO_CLICK_TIMESTAMP = Long.MIN_VALUE

    fun Modifier.singleClick(
        enabled: Boolean = true,
        interval: Long = DEFAULT_SINGLE_CLICK_INTERVAL,
        onClickLabel: String? = null,
        role: Role? = null,
        onClick: () -> Unit,
    ): Modifier = composed {
        require(interval >= 0) { "interval must not be negative" }
        val currentOnClick by rememberUpdatedState(onClick)
        var lastClickTimestamp by remember(interval) { mutableLongStateOf(NO_CLICK_TIMESTAMP) }

        clickable(
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
        ) {
            val currentTimestamp = SystemClock.elapsedRealtime()
            if (lastClickTimestamp == NO_CLICK_TIMESTAMP || currentTimestamp - lastClickTimestamp >= interval) {
                lastClickTimestamp = currentTimestamp
                currentOnClick()
            }
        }
    }

    @Composable
    fun rememberSingleClick(
        interval: Long = DEFAULT_SINGLE_CLICK_INTERVAL,
        onClick: () -> Unit,
    ): () -> Unit {
        require(interval >= 0) { "interval must not be negative" }
        val currentOnClick by rememberUpdatedState(onClick)
        var lastClickTimestamp by remember(interval) { mutableLongStateOf(NO_CLICK_TIMESTAMP) }

        return {
            val currentTimestamp = SystemClock.elapsedRealtime()
            if (lastClickTimestamp == NO_CLICK_TIMESTAMP || currentTimestamp - lastClickTimestamp >= interval) {
                lastClickTimestamp = currentTimestamp
                currentOnClick()
            }
        }
    }
}