package com.climbingholddetector.presentation.fullscreen

import androidx.compose.animation.Animatable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.Velocity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class FullscreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel(), FullscreenScreenListener {

    private val _offset = MutableStateFlow(Offset.Zero)
    private val _offsetAnimateToVelocity = MutableStateFlow<Velocity?>(null)
    private val _scale = MutableStateFlow(1F)
    val offset: StateFlow<Offset> = _offset
    val offsetAnimateToVelocity: StateFlow<Velocity?> = _offsetAnimateToVelocity
    val scale: StateFlow<Float> = _scale

    private val velocityTracker = VelocityTracker()

    override fun onGesture(centroid: Offset, panChange: Offset, zoomChange: Float, rotationChange: Float) {
        //  println("here $offset and $offsetX and $offsetY")
        _offsetAnimateToVelocity.value = null
        _offset.value += panChange

        velocityTracker.addPosition(
            System.currentTimeMillis(),
            _offset.value,
        )

        _scale.value = (_scale.value * zoomChange).coerceIn(SCALE_MIN, SCALE_MAX)
    }

    override fun onMeasured(containerHeightPx: Int, containerWidthPx: Int, imageHeightPx: Int, imageWidthPx: Int) {

    }

    companion object {
        private const val SCALE_MIN = 1F
        private const val SCALE_MAX = 5F
    }
}