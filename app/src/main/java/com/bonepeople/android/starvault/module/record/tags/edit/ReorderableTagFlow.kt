package com.bonepeople.android.starvault.module.record.tags.edit

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch

object ReorderableTagFlow {
    /**
     * 支持拖动排序的标签流式布局。
     *
     * 交互效果：
     * - 长按某个标签后进入拖动状态，被拖动的标签会跟随手指移动，并轻微放大后浮在最上层。
     * - 其余标签会自动流动到新的位置，给被拖动的标签让出空隙，移动过程带有弹性动画。
     * - 手指抬起后，被拖动的标签平滑归位到最终槽位，顺序变化通过 [onMove] 回调给上层。
     *
     * 排序的数据源由上层持有（[tags]），本组件只负责在拖动过程中根据手指位置计算目标位置并回调 [onMove]，
     * 真正的列表顺序由上层更新后重新传入，从而保持单一数据源。
     */
    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun Content(
        tags: List<String>,
        onMove: (from: Int, to: Int) -> Unit,
        modifier: Modifier = Modifier,
        horizontalSpacing: Dp = 6.dp,
        verticalSpacing: Dp = 4.dp,
        chipContent: @Composable (tag: String) -> Unit,
    ) {
        val haptic = LocalHapticFeedback.current
        val scope = rememberCoroutineScope()
        // 当前被拖动的标签，null 表示没有拖动
        var draggingTag by remember { mutableStateOf<String?>(null) }
        // 被拖动标签左上角在容器坐标系中的实时位置
        var fingerOffset by remember { mutableStateOf(Offset.Zero) }
        // 手指触点在容器坐标系中的实时位置，用于判断目标标签
        var pointerPosition by remember { mutableStateOf(Offset.Zero) }
        // 被拖动标签的缩放比例，长按时立即放大，归位时随位置同步恢复
        var draggingScale by remember { mutableStateOf(1f) }
        // 每个标签在流式布局中的稳定槽位矩形，用于拖动时的命中判断
        val slotRects = remember { mutableMapOf<String, Rect>() }
        // 通过 rememberUpdatedState 让手势回调始终读取到最新的列表与回调
        val latestTags by rememberUpdatedState(tags)
        val latestOnMove by rememberUpdatedState(onMove)

        // 拖动结束后将标签平滑归位到它的最终槽位，再退出拖动状态
        fun settleToSlot() {
            val dragged = draggingTag ?: return
            val target = slotRects[dragged]?.topLeft ?: fingerOffset
            val start = fingerOffset
            val startScale = draggingScale
            scope.launch {
                animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        visibilityThreshold = 0.001f,
                    ),
                ) { progress, _ ->
                    fingerOffset = start + (target - start) * progress
                    draggingScale = startScale + (1f - startScale) * progress
                }
                fingerOffset = target
                draggingScale = 1f
                draggingTag = null
            }
        }

        FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        ) {
            tags.forEach { tag ->
                key(tag) {
                    val isDragging = tag == draggingTag
                    // 该标签当前的槽位左上角，供被拖动标签计算跟随手指所需的位移
                    var slot by remember { mutableStateOf(Offset.Zero) }
                    Box(
                        modifier = Modifier
                            .zIndex(if (isDragging) 1f else 0f)
                            // 放在动画位移之外，捕获到的是稳定的目标槽位（不受归位动画影响）
                            .onGloballyPositioned { coordinates ->
                                val position = coordinates.positionInParent()
                                slot = position
                                slotRects[tag] = Rect(
                                    offset = position,
                                    size = Size(coordinates.size.width.toFloat(), coordinates.size.height.toFloat()),
                                )
                            }
                            .then(
                                if (isDragging) {
                                    // 被拖动标签通过同一个动画进度同步改变位置和大小
                                    Modifier.graphicsLayer {
                                        translationX = fingerOffset.x - slot.x
                                        translationY = fingerOffset.y - slot.y
                                        scaleX = draggingScale
                                        scaleY = draggingScale
                                    }
                                } else {
                                    // 其余标签：槽位变化时平滑滑动，让出空隙
                                    Modifier.animatePlacement()
                                },
                            )
                            .pointerInput(tag) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { touchOffset ->
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        val tagPosition = slotRects[tag]?.topLeft ?: slot
                                        fingerOffset = tagPosition
                                        pointerPosition = tagPosition + touchOffset
                                        draggingScale = 1.05f
                                        draggingTag = tag
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        fingerOffset += dragAmount
                                        pointerPosition += dragAmount
                                        val dragged = draggingTag
                                        if (dragged != null) {
                                            val current = latestTags
                                            val from = current.indexOf(dragged)
                                            val to = current.indexOfFirst { candidate ->
                                                candidate != dragged && slotRects[candidate]?.contains(pointerPosition) == true
                                            }
                                            if (from >= 0 && to >= 0 && to != from) {
                                                latestOnMove(from, to)
                                            }
                                        }
                                    },
                                    onDragEnd = { settleToSlot() },
                                    onDragCancel = { settleToSlot() },
                                )
                            },
                    ) {
                        chipContent(tag)
                    }
                }
            }
        }
    }

    /**
     * 当组件被父布局放置到新位置时，以弹性动画平滑过渡，而不是直接跳变。
     * 用于标签让位时的滑动效果。
     */
    private fun Modifier.animatePlacement(): Modifier = composed {
        val scope = rememberCoroutineScope()
        val animatable = remember { Animatable(IntOffset.Zero, IntOffset.VectorConverter) }
        // null 表示尚未首次放置
        var targetOffset by remember { mutableStateOf<IntOffset?>(null) }
        var placementInitialized by remember { mutableStateOf(false) }
        this
            .onPlaced { coordinates ->
                val newTarget = coordinates.positionInParent().round()
                val firstPlacement = targetOffset == null
                targetOffset = newTarget
                if (firstPlacement) {
                    // snapTo 在协程中执行；完成前保持零偏移，避免短暂使用原点坐标而闪烁
                    scope.launch {
                        animatable.snapTo(newTarget)
                        placementInitialized = true
                    }
                }
            }
            .offset {
                val target = targetOffset ?: IntOffset.Zero
                if (!placementInitialized) {
                    return@offset IntOffset.Zero
                }
                if (animatable.targetValue != target) {
                    scope.launch { animatable.animateTo(target, spring(stiffness = Spring.StiffnessMediumLow)) }
                }
                animatable.value - target
            }
    }
}