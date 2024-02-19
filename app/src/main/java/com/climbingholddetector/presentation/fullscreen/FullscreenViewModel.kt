package com.climbingholddetector.presentation.fullscreen


import androidx.compose.animation.SplineBasedFloatDecayAnimationSpec
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.generateDecayAnimationSpec
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.Density
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.climbingholddetector.presentation.fullscreen.models.FullscreenGestureListener
import com.climbingholddetector.presentation.fullscreen.models.FullscreenImageListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FullscreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel(), FullscreenGestureListener, FullscreenImageListener {

    //region Observable View State
    private val _offsetX = MutableStateFlow(value = OFFSET_INITIAL)
    private val _offsetY = MutableStateFlow(value = OFFSET_INITIAL)
    private val _scale = MutableStateFlow(value = SCALE_MIN)
    val offsetX: StateFlow<Float> = _offsetX
    val offsetY: StateFlow<Float> = _offsetY
    val scale: StateFlow<Float> = _scale
    //endregion Observable View State

    //region Private ViewModel Fields
    private val offsetAnimatableX = Animatable(_offsetX.value)
    private val offsetAnimatableY = Animatable(_offsetY.value)
    private val velocityTracker = VelocityTracker()
    //endregion Private ViewModel Fields

    //region FullscreenGestureListener
    override fun onGesturesAllStopped(density: Density, scope: CoroutineScope) {
        val velocity = velocityTracker.calculateVelocity()
        velocityTracker.resetTracking()

        val decay = SplineBasedFloatDecayAnimationSpec(density).generateDecayAnimationSpec<Float>()
        scope.launch {
            offsetAnimatableX.snapTo(_offsetX.value)
            offsetAnimatableX.animateDecay(velocity.x, decay) { _offsetX.value = value }
        }
        scope.launch {
            offsetAnimatableY.snapTo(_offsetY.value)
            offsetAnimatableY.animateDecay(velocity.y, decay) { _offsetY.value = value }
        }
    }

    override fun onGesturesAnyStarted() {
        viewModelScope.launch { offsetAnimatableX.stop() }
        viewModelScope.launch { offsetAnimatableY.stop() }
        velocityTracker.resetTracking()
    }

    override fun onGestureTransformation(
        centroid: Offset,
        panChange: Offset,
        zoomChange: Float,
        rotationChange: Float
    ) {
        val newOffset = Offset(x = _offsetX.value, y = _offsetY.value) + panChange
        _offsetX.value = newOffset.x
        _offsetY.value = newOffset.y

        velocityTracker.addPosition(
            timeMillis = System.currentTimeMillis(),
            position = newOffset,
        )

        _scale.value = (_scale.value * zoomChange).coerceIn(SCALE_MIN, SCALE_MAX)
    }
    //endregion FullscreenGestureListener

    //region FullscreenImageListener
    override fun onImageMeasured(containerHeightPx: Int, containerWidthPx: Int, imageHeightPx: Int, imageWidthPx: Int) {

    }
    //endregion FullscreenImageListener

    companion object {
        private const val OFFSET_INITIAL = 0F
        private const val SCALE_MIN = 1F
        private const val SCALE_MAX = 5F
    }
}