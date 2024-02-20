package com.sprayWallz.presentation.fullscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sprayWallz.R
import com.sprayWallz.presentation.fullscreen.models.FullscreenGestureListener
import com.sprayWallz.presentation.fullscreen.models.FullscreenImageListener
import kotlin.math.roundToInt

@Composable
fun FullscreenScreen(
    viewModel: FullscreenViewModel
) {
    FullscreenScreenContent(
        fullscreenGestureListener = viewModel,
        fullscreenImageListener = viewModel,
        imageHeightPx = viewModel.imageHeightPx.collectAsState().value,
        imageWidthPx = viewModel.imageWidthPx.collectAsState().value,
        offsetX = viewModel.offsetX.collectAsState().value,
        offsetY = viewModel.offsetY.collectAsState().value,
        scale = viewModel.scale.collectAsState().value, // todo lifecycle
    )
}

@Composable
fun FullscreenScreenContent(
    fullscreenGestureListener: FullscreenGestureListener,
    fullscreenImageListener: FullscreenImageListener,
    imageWidthPx: Float,
    imageHeightPx: Float,
    offsetX: Float,
    offsetY: Float,
    scale: Float,
) {
    BoxWithConstraints(
        modifier = Modifier.background(Color.DarkGray)
    ) {
        val painter = painterResource(id = R.drawable.download)
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        Box(
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


                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                contentScale = ContentScale.Fit,
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .width(density.run { imageWidthPx.toDp() })
                    .height(density.run { imageHeightPx.toDp() })
                    .graphicsLayer {
                        translationX = offsetX
                        translationY = offsetY
                        scaleX = scale
                        scaleY = scale
                    }
                    .shadow(elevation = 16.dp)

            )
        }

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