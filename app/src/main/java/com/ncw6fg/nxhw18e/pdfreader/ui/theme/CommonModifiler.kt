package com.ncw6fg.nxhw18e.pdfreader.ui.theme

import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.ncw6fg.nxhw18e.pdfreader.R
import com.ncw6fg.nxhw18e.pdfreader.ui.act.SearchActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * create by colin
 * 2026/2/4
 */

val fillMax = Modifier
    .fillMaxSize()


fun Modifier.shimmerHighlight(): Modifier = composed {
    // 保存按钮的宽度（像素）
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val brushWidth = size.width / 5f
    // 动态计算动画范围：从负的宽度开始，到宽度的两倍结束
    val translateAnim by transition.animateFloat(
        initialValue = -brushWidth,
        targetValue = size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translation"
    )

    this
        .onSizeChanged { size = it } // 获取实际尺寸
        .drawWithContent { // 在绘制层处理，性能更好
            drawContent() // 先画按钮原本的内容（文字、背景）
            if (size.width > 0) {
                val brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0f),
                        Color.White.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0f),
                    ),
                    // 使用动态的动画值
                    start = Offset(translateAnim, translateAnim),
                    end = Offset(translateAnim + brushWidth, translateAnim + brushWidth)
                )
                // 绘制闪光层
                drawRect(brush = brush)
            }
        }
}

@Composable
fun CommonSpace(width: Dp = 8.dp, height: Dp = 8.dp) {
    Spacer(Modifier.size(width, height))
}


@Composable
fun ShimmerButton(
    modifier: Modifier,
    text: String,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(0.dp)
    ) {
        // 使用 Box 容器来叠加闪光层
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shimmerHighlight(), // 应用闪光动画
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopBar(
    title: String = stringResource(R.string.app_name),
    goBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (goBack != null) {
                IconButton(onClick = {
                    goBack()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back"
                    )
                }
            }
        },
        actions = {
            IconButton(content = {
                Icon(
                    imageVector = Icons.Default.Search, contentDescription = "search"
                )
            }, onClick = {
                context.startActivity(Intent(context, SearchActivity::class.java))
            })
        })
}


@Composable
fun <T> ReorderableColumn(
    items: List<T>,
    itemContent: @Composable (item: T, isDragging: Boolean, index: Int) -> Unit,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current

    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var draggingOffset by remember { mutableFloatStateOf(0f) }
    val autoScrollJob = remember { mutableStateOf<Job?>(null) }
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        // 根据点击坐标找到对应的 item 索引
                        listState.layoutInfo.visibleItemsInfo
                            .firstOrNull { item ->
                                offset.y.toInt() in item.offset..(item.offset + item.size)
                            }
                            ?.also {
                                draggedIndex = it.index
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        draggingOffset += dragAmount.y

                        // 逻辑：寻找当前拖拽到的目标位置
                        val currentDraggedItem = draggedIndex?.let { index ->
                            listState.layoutInfo.visibleItemsInfo.find { it.index == index }
                        } ?: return@detectDragGesturesAfterLongPress

                        val startOffset = currentDraggedItem.offset + draggingOffset
                        val endOffset = startOffset + currentDraggedItem.size

                        // 寻找并交换位置
                        listState.layoutInfo.visibleItemsInfo
                            .find { item ->
                                // 只有目标项不是自己，且中点位置交叉时触发交换
                                val itemMidPoint = item.offset + item.size / 2
                                draggedIndex != item.index && itemMidPoint.toFloat() in startOffset..endOffset
                            }
                            ?.also { targetItem ->
                                val from = draggedIndex!!
                                val to = targetItem.index
                                onMove(from, to)
                                draggedIndex = to
                                draggingOffset += (currentDraggedItem.offset - targetItem.offset).toFloat()
                            }

                        // 自动滚动处理：靠近顶部或底部时自动滚动
                        val viewportHeight = listState.layoutInfo.viewportEndOffset
                        if (endOffset > viewportHeight - 100f) {
                            if (autoScrollJob.value == null) autoScrollJob.value =
                                scope.launch { listState.animateScrollBy(200f) }
                        } else if (startOffset < 100f) {
                            if (autoScrollJob.value == null) autoScrollJob.value =
                                scope.launch { listState.animateScrollBy(-200f) }
                        } else {
                            autoScrollJob.value?.cancel()
                            autoScrollJob.value = null
                        }
                    },
                    onDragEnd = {
                        draggedIndex = null
                        draggingOffset = 0f
                        autoScrollJob.value?.cancel()
                        autoScrollJob.value = null
                    },
                    onDragCancel = {
                        draggedIndex = null
                        draggingOffset = 0f
                        autoScrollJob.value?.cancel()
                        autoScrollJob.value = null
                    }
                )
            }
    ) {
        itemsIndexed(items) { index, item ->
            val isDragging = index == draggedIndex
            val zIndex = if (isDragging) 1f else 0f
            // 拖拽时的视觉效果反馈
            // val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
            // val scale by animateFloatAsState(if (isDragging) 1.05f else 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(zIndex) // 确保被拖拽的项在最上层
                    .graphicsLayer {
                        translationY = if (isDragging) draggingOffset else 0f
                        // scaleX = scale
                        // scaleY = scale
                    }
                // .shadow(elevation)
            ) {
                itemContent(item, isDragging, index)
            }
        }
    }
}

@Composable
fun AdDialog() {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(Color.White, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.ad_loading), fontSize = 14.sp)
            }
        }
    }
}
