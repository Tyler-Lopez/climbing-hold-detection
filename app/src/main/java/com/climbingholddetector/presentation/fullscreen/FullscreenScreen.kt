package com.climbingholddetector.presentation.fullscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import com.climbingholddetector.R
import com.climbingholddetector.presentation.fullscreen.models.FullscreenGestureListener
import com.climbingholddetector.presentation.fullscreen.models.FullscreenImageListener
import kotlin.math.roundToInt

@Composable
fun FullscreenScreen(
    viewModel: FullscreenViewModel
) {
    FullscreenScreenContent(
        fullscreenGestureListener = viewModel,
        fullscreenImageListener = viewModel,
        offsetX = viewModel.offsetX.collectAsState().value,
        offsetY = viewModel.offsetY.collectAsState().value,
        scale = viewModel.scale.collectAsState().value, // todo lifecycle
    )
}

@Composable
fun FullscreenScreenContent(
    fullscreenGestureListener: FullscreenGestureListener,
    fullscreenImageListener: FullscreenImageListener,
    offsetX: Float,
    offsetY: Float,
    scale: Float,
) {
    BoxWithConstraints {
        val painter = painterResource(id = R.drawable.download)
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        Image(
            contentScale = ContentScale.Fit,
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures(
                        panZoomLock = false,
                        onGesture = fullscreenGestureListener::onGestureTransformation
                    )
                }
                .pointerInput(Unit) {
                    forEachGesture {
                        awaitPointerEventScope {
                            awaitFirstDown(requireUnconsumed = false)
                            fullscreenGestureListener.onGesturesAnyStarted()
                            do {
                                val event = awaitPointerEvent()
                                val canceled = event.changes.any { it.consumed.positionChange }
                            } while (!canceled && event.changes.any { it.pressed })
                            fullscreenGestureListener.onGesturesAllStopped(
                                density = this,
                                scope = scope,
                            )
                        }
                    }
                }
                .graphicsLayer {
                    translationX = offsetX
                    translationY = offsetY
                    scaleX = scale
                    scaleY = scale
                }
        )

        LaunchedEffect(Unit) {
            fullscreenImageListener.onImageMeasured(
                containerHeightPx = density.run { maxHeight.roundToPx() },
                containerWidthPx = density.run { maxWidth.roundToPx() },
                imageHeightPx = painter.intrinsicSize.height.roundToInt(),
                imageWidthPx = painter.intrinsicSize.width.roundToInt(),
            )
        }
    }
}