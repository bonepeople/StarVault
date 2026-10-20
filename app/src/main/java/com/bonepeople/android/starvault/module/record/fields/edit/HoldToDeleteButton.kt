package com.bonepeople.android.starvault.module.record.fields.edit

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal object HoldToDeleteButton {
    @Composable
    fun Content(onHoldComplete: () -> Unit, modifier: Modifier = Modifier) {
        val interactionSource = remember { MutableInteractionSource() }
        val progress = remember { Animatable(0f) }
        val currentOnHoldComplete by rememberUpdatedState(onHoldComplete)
        val progressColor = MaterialTheme.colorScheme.errorContainer
        val shape = RoundedCornerShape(20.dp)
        LaunchedEffect(interactionSource) {
            var activePress: PressInteraction.Press? = null
            var progressJob: Job? = null
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        activePress = interaction
                        progressJob?.cancel()
                        progress.snapTo(0.5f)
                        progressJob = launch {
                            progress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = DELETE_HOLD_DURATION, easing = LinearEasing),
                            )
                            currentOnHoldComplete()
                        }
                    }
                    is PressInteraction.Release -> {
                        if (interaction.press == activePress) {
                            activePress = null
                            progressJob?.cancel()
                            progressJob = launch {
                                progress.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(durationMillis = DELETE_RESET_DURATION, easing = LinearEasing),
                                )
                            }
                        }
                    }
                    is PressInteraction.Cancel -> {
                        if (interaction.press == activePress) {
                            activePress = null
                            progressJob?.cancel()
                            progressJob = launch {
                                progress.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(durationMillis = DELETE_RESET_DURATION, easing = LinearEasing),
                                )
                            }
                        }
                    }
                }
            }
        }
        Box(
            modifier = modifier
                .defaultMinSize(minHeight = 40.dp)
                .clip(shape)
                .drawWithContent {
                    drawRect(
                        color = progressColor,
                        size = Size(width = size.width * progress.value, height = size.height),
                    )
                    drawContent()
                }
                .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = {},
                ),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "删除字段",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }

    private const val DELETE_HOLD_DURATION = 1_000
    private const val DELETE_RESET_DURATION = 600
}