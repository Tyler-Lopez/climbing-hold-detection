package com.climbingholddetector.presentation.fullscreen

import androidx.annotation.Px
import androidx.compose.ui.geometry.Offset
import javax.annotation.concurrent.Immutable

@Immutable
interface FullscreenScreenListener {
    fun onGesture(
        centroid: Offset,
        panChange: Offset,
        zoomChange: Float,
        rotationChange: Float
    )
    fun onMeasured(
        @Px containerHeightPx: Int,
        @Px containerWidthPx: Int,
        @Px imageHeightPx: Int,
        @Px imageWidthPx: Int,
    )
}