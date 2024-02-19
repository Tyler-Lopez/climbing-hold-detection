package com.climbingholddetector.presentation.fullscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import com.climbingholddetector.R
import kotlin.math.roundToInt

@Composable
fun FullscreenScreen(
    viewModel: FullscreenViewModel
) {
    FullscreenScreenContent(
        fullscreenScreenListener = viewModel,
        offset = viewModel.offset.collectAsState().value, // todo lifecycle
        scale = viewModel.scale.collectAsState().value, // todo lifecycle
    )
}

@Composable
fun FullscreenScreenContent(
    fullscreenScreenListener: FullscreenScreenListener,
    offset: Offset,
    scale: Float,
) {
    BoxWithConstraints {
        val painter = painterResource(id = R.drawable.download)

        val velocityTracker = VelocityTracker()
        velocityTracker.calculateVelocity()
        val density = LocalDensity.current
        Image(
            contentScale = ContentScale.Fit,
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures(
                        panZoomLock = false,
                        onGesture = fullscreenScreenListener::onGesture
                    )
                }
                .graphicsLayer {
                    translationX = offset.x
                    translationY = offset.y
                    scaleX = scale
                    scaleY = scale
                }
        )

        LaunchedEffect(Unit) {
            fullscreenScreenListener.onMeasured(
                containerHeightPx = density.run { maxHeight.roundToPx() },
                containerWidthPx = density.run { maxWidth.roundToPx() },
                imageHeightPx = painter.intrinsicSize.height.roundToInt(),
                imageWidthPx = painter.intrinsicSize.width.roundToInt()
            )
        }
    }


    /*
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // coroutineScope {
                detectTransformGestures(
                    panZoomLock = false,
                    onGesture = { centroid, pan, gestureZoom, gestureRotate ->
                        //  println("here $offset and $offsetX and $offsetY")
                        transformIsInProgress = true
                        val targetOffsetX = offsetX.value + pan.x
                        val targetOffsetY = offsetY.value + pan.y

                        snapOffset(targetOffsetX, targetOffsetY)
                        velocityTracker.addPosition(
                            System.currentTimeMillis(),
                            Offset(targetOffsetX, targetOffsetY)
                        )

                        scale = (scale * gestureZoom).coerceIn(1F, 5F)


                        val scaledWidthPx = (widthPx) * scale
                        val scaledWidthExcessPx = scaledWidthPx - (widthPx)
                        val scaledWidthExcessHalvedPx = scaledWidthExcessPx / 2F
                    }
                )
            }
            .pointerInput(Unit) {
                val decay = splineBasedDecay<Float>(this)

                forEachGesture {
                    awaitPointerEventScope {
                        awaitFirstDown(requireUnconsumed = false)
                        velocityTracker.resetTracking()
                        scope.launch {
                            offsetX.stop()
                            offsetY.stop()
                        }
                        do {
                            val event = awaitPointerEvent()
                            val canceled = event.changes.any { it.consumed.positionChange }
                        } while (!canceled && event.changes.any { it.pressed })
                        val velocity = velocityTracker.calculateVelocity()
                        scope.launch { offsetX.animateDecay(velocity.x, decay) }
                        scope.launch { offsetY.animateDecay(velocity.y, decay) }
                    }
                }
            }
            .background(Color.Red)
    ) {

        val offsetX = remember { Animatable(0F) }
        val offsetY = remember { Animatable(0F) }

        val scope = rememberCoroutineScope()
        fun snapOffset(targetOffsetX: Float, targetOffsetY: Float) {
            scope.launch {
                offsetX.snapTo(targetOffsetX)
                offsetY.snapTo(targetOffsetY)
            }
        }

        val painter = painterResource(id = R.drawable.download)


        var scale by remember { mutableStateOf(1F) }
        var transformIsInProgress by remember { mutableStateOf(false) }
        val state = rememberTransformableState { zoomChange: Float, panChange: Offset, rotationChange: Float ->
            scale *= zoomChange
        }

        val widthPx = LocalDensity.current.run { maxWidth.roundToPx() }
        val heightPx = LocalDensity.current.run { maxHeight.roundToPx() }

        val imageWidthPx = painter.intrinsicSize.width
        val imageHeightPx = painter.intrinsicSize.height

        println("image size is ${painter.intrinsicSize}")


        var size: Size
        size = if (true) {
            println("image width is better than height")
            val imageHeightToScreenHeight = widthPx / imageWidthPx
            println("yo is $imageHeightToScreenHeight")
            Size(widthPx.toFloat(), imageHeightToScreenHeight * imageHeightPx)
        } else {
            println("otherwise")
            val imageWidthToScreenWidth = imageWidthPx / widthPx
            Size(imageWidthToScreenWidth * imageWidthPx, heightPx.toFloat())
        }
        println("here... size is $size")

// Used to calculate fling decay.
        val velocityTracker = VelocityTracker()


        Image(
            contentScale = ContentScale.Fit,
            painter = painter,
            contentDescription = "",
            modifier = Modifier
                .size(LocalDensity.current.run { size.width.toDp() }, LocalDensity.current.run { size.height.toDp() })
                .graphicsLayer {
                    translationX = offsetX.value
                }
                .scale(scale = scale)
                .background(Color.Red)
                .background(Color.Cyan)
        )

    }

     */

}