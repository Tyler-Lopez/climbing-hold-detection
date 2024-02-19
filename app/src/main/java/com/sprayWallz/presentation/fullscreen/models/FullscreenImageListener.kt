package com.sprayWallz.presentation.fullscreen.models

import androidx.annotation.Px
import javax.annotation.concurrent.Immutable

@Immutable
fun interface FullscreenImageListener {
    fun onImageMeasured(
        @Px containerHeightPx: Int,
        @Px containerWidthPx: Int,
        @Px imageHeightPx: Int,
        @Px imageWidthPx: Int,
    )
}