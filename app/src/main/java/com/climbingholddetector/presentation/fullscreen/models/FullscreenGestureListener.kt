package com.climbingholddetector.presentation.fullscreen.models

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineScope
import javax.annotation.concurrent.Immutable

@Immutable
interface FullscreenGestureListener {
    fun onGesturesAnyStarted()
    fun onGesturesAllStopped(
        density: Density,
        scope: CoroutineScope,
    )

    fun onGestureTransformation(
        centroid: Offset,
        panChange: Offset,
        zoomChange: Float,
        rotationChange: Float
    )
}