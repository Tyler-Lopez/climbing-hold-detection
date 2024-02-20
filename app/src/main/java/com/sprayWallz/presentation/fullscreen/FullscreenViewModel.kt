package com.sprayWallz.presentation.fullscreen


import androidx.annotation.Px
import androidx.compose.animation.SplineBasedFloatDecayAnimationSpec
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.generateDecayAnimationSpec
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.Density
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sprayWallz.presentation.fullscreen.models.FullscreenGestureListener
import com.sprayWallz.presentation.fullscreen.models.FullscreenImageListener
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
    private val _imageWidthPx = MutableStateFlow(value = 0F)
    private val _imageHeightPx = MutableStateFlow(value = 0F)
    private val _offsetX = MutableStateFlow(value = OFFSET_INITIAL)
    private val _offsetY = MutableStateFlow(value = OFFSET_INITIAL)
    private val _scale = MutableStateFlow(value = SCALE_MIN)
    val imageWidthPx: StateFlow<Float> = _imageWidthPx
    val imageHeightPx: StateFlow<Float> = _imageHeightPx
    val offsetX: StateFlow<Float> = _offsetX
    val offsetY: StateFlow<Float> = _offsetY
    val scale: StateFlow<Float> = _scale
    //endregion Observable View State

    //region Private ViewModel Fields
    private val offsetAnimatableX = Animatable(_offsetX.value)
    private val offsetAnimatableY = Animatable(_offsetY.value)
    private val velocityTracker = VelocityTracker()
    private var containerHeightPx: Float = 0F
    private var containerWidthPx: Float = 0F
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
        val offset = Offset(x = _offsetX.value, y = _offsetY.value)
        velocityTracker.addPosition(System.currentTimeMillis(), offset)
    }

    override fun onGestureTransformation(
        centroid: Offset,
        panChange: Offset,
        zoomChange: Float,
        rotationChange: Float
    ) {
        val newScale = (_scale.value * zoomChange).coerceIn(SCALE_MIN, SCALE_MAX)
        val oldScale = _scale.value
        _scale.value = newScale
        if (oldScale != newScale) {
            updateBounds()
        }

        val newOffset = Offset(x = _offsetX.value, y = _offsetY.value) + panChange
        _offsetX.value = newOffset.x.coerceIn(offsetAnimatableX.lowerBound, offsetAnimatableX.upperBound)
        _offsetY.value = newOffset.y.coerceIn(offsetAnimatableY.lowerBound, offsetAnimatableY.upperBound)

        velocityTracker.addPosition(
            timeMillis = System.currentTimeMillis(),
            position = newOffset,
        )
    }
    //endregion FullscreenGestureListener

    //region FullscreenImageListener
    override fun onImageMeasured(containerHeightPx: Int, containerWidthPx: Int, imageHeightPx: Int, imageWidthPx: Int) {
        this.containerHeightPx = containerHeightPx.toFloat()
        this.containerWidthPx = containerWidthPx.toFloat()

        val ratioContainer = this.containerWidthPx / this.containerHeightPx
        val ratioImage = imageWidthPx.toFloat() / imageHeightPx.toFloat()

        if (ratioContainer > ratioImage) {
            _imageHeightPx.value = this.containerHeightPx
            _imageWidthPx.value = _imageHeightPx.value * ratioImage
        } else {
            _imageWidthPx.value = this.containerWidthPx
            _imageHeightPx.value = _imageWidthPx.value / ratioImage
        }

        updateBounds()
    }
    //endregion FullscreenImageListener

    //region Utility Functions
    private fun updateBounds() {
        val scaledImageWidth = _imageWidthPx.value * _scale.value
        val excessImageWidthHalved = (scaledImageWidth - containerWidthPx) / 2F

        offsetAnimatableX.updateBounds(
            lowerBound = (excessImageWidthHalved * -1F).coerceAtMost(maximumValue = 0F),
            upperBound = (excessImageWidthHalved * 1F).coerceAtLeast(minimumValue = 0F),
        )

        val scaledImageHeight = _imageHeightPx.value * _scale.value
        val excessImageHeightHalved = (scaledImageHeight - containerHeightPx) / 2F

        offsetAnimatableY.updateBounds(
            lowerBound = (excessImageHeightHalved * -1F).coerceAtMost(maximumValue = 0F),
            upperBound = (excessImageHeightHalved * 1F).coerceAtLeast(minimumValue = 0F),
        )
    }
    //endregion Utility Functions

    companion object {
        @Px private const val OFFSET_INITIAL = 0F
        private const val SCALE_MIN = 1F
        private const val SCALE_MAX = 5F
    }
}