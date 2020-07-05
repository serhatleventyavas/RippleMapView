package com.serhatleventyavas.ripplemapview

import android.animation.IntEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*

class RippleMapView private constructor(
        private var context: Context,
        private var googleMap: GoogleMap,
        private var latLng: LatLng = LatLng(0.0, 0.0),
        private var distance: Double = 200.0,
        private var transparency: Float = 0.12f,
        private var numberOfRipples: Int = 1,
        private var fillColor: Int = Color.TRANSPARENT,
        private var strokeColor: Int = Color.TRANSPARENT,
        private var strokeWidth: Int = 10,
        private var durationBetweenTwoRipples: Long = 4000,
        private var rippleDuration: Long = 12000): LifecycleObserver {

    private var prevLatLng: LatLng? = null
    private var mAnimators: Array<ValueAnimator> = arrayOf()
    private var mOverlays: Array<GroundOverlay> = arrayOf()
    private val drawable: GradientDrawable = ContextCompat.getDrawable(context, R.drawable.ripple_background) as GradientDrawable
    private var isAnimationRunning = false
    private var isCallStartAnimation: Boolean = false
    private var isInitializeCompleted: Boolean = false

    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    init {
        scope.launch {
            val density = Resources.getSystem().displayMetrics.density
            val width = (strokeWidth * density).toInt()
            drawable.setColor(fillColor)
            drawable.setStroke(width, strokeColor)
            val tempBackground = scope.async(Dispatchers.Default) {
                val backgroundImage = drawableToBitmap(drawable)
                BitmapDescriptorFactory.fromBitmap(backgroundImage)
            }

            val bitmapBackground = tempBackground.await()

            mAnimators = Array(numberOfRipples) {
                ValueAnimator.ofInt(0, distance.toInt())
            }

            mOverlays = Array(numberOfRipples) {
                googleMap.addGroundOverlay(GroundOverlayOptions()
                        .position(latLng, distance.toFloat())
                        .transparency(transparency)
                        .visible(false)
                        .image(bitmapBackground))
            }
            isInitializeCompleted = true
            if (isCallStartAnimation) {
                isCallStartAnimation = false
                withContext(Dispatchers.Main) {
                    startRippleMapAnimation()
                }
            }
        }
    }

    fun startRippleMapAnimation() {
        if (isAnimationRunning) {
            return
        }

        if (!isInitializeCompleted) {
            isCallStartAnimation = true
            return
        }
        for (index in 0 until numberOfRipples) {
            overLay(index)
        }
        isAnimationRunning = true
    }

    private fun stopRippleMapAnimation() {
        try {
            for (index in 0 until numberOfRipples) {
                mAnimators[index].cancel()
                mOverlays[index].remove()
            }
        } catch (e: Exception) {
            Log.e("RippleMapView", "Error", e)
        } finally {
            isAnimationRunning = false
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable
            if (bitmapDrawable.bitmap != null) {
                return bitmapDrawable.bitmap
            }
        }
        val bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        } else {
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun overLay(index: Int) {
        if (index > 0)
            mAnimators[index].startDelay = durationBetweenTwoRipples
        mAnimators[index].repeatCount = ValueAnimator.INFINITE
        mAnimators[index].repeatMode = ValueAnimator.RESTART
        mAnimators[index].duration = rippleDuration
        mAnimators[index].setEvaluator(IntEvaluator())
        mAnimators[index].interpolator = LinearInterpolator()
        mAnimators[index].addUpdateListener { valueAnimator: ValueAnimator ->
            val animatedFraction = valueAnimator.animatedFraction
            if (!mOverlays[index].isVisible) {
                mOverlays[index].isVisible = true
            }
            mOverlays[index].transparency = animatedFraction
            val animatedValue = valueAnimator.animatedValue as Int
            mOverlays[index].setDimensions(animatedValue.toFloat())
            if (distance - animatedValue <= 10) {
                if (latLng !== prevLatLng) {
                    mOverlays[index].position = latLng
                }
            }
        }
        mAnimators[index].start()
    }

    // LIFECYCLE EVENTS


    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        if (isAnimationRunning) {
            stopRippleMapAnimation()
        }
        scope.cancel()
    }

    // GETTER SETTER FUNCTIONS
    fun isAnimationRunning(): Boolean {
        return isAnimationRunning
    }

    fun withRippleDuration(rippleDuration: Long) {
        this.rippleDuration = rippleDuration
    }

    fun withTransparency(transparency: Float) {
        this.transparency = transparency
    }

    fun withDistance(distance: Double) {
        if (distance < 200) {
            this.distance = 200.0
        } else {
            this.distance = distance
        }
    }

    fun withLatLng(latLng: LatLng) {
        this.latLng = latLng
    }

    fun withDurationBetweenTwoRipples(durationBetweenTwoRipples: Long) {
        this.durationBetweenTwoRipples = durationBetweenTwoRipples
    }

    // BUILDER CLASS //
    data class Builder(
            var context: Context,
            var googleMap: GoogleMap,
            var latLng: LatLng = LatLng(0.0, 0.0),
            var distance: Double = 200.0,
            var transparency: Float = 0.12f,
            var numberOfRipples: Int = 1,
            var fillColor: Int = Color.TRANSPARENT,
            var strokeColor: Int = Color.TRANSPARENT,
            var strokeWidth: Int = 10,
            var durationBetweenTwoRipples: Long = 4000,
            var rippleDuration: Long = 12000) {

        fun transparency(transparency: Float) = apply {
            this.transparency = transparency
        }

        fun latLng(latLng: LatLng) = apply {
            this.latLng = latLng
        }

        fun distance(distance: Double) = apply {
            this.distance = distance
        }

        fun numberOfRipples(numberOfRipples: Int) = apply {
            this.numberOfRipples = numberOfRipples
        }

        fun fillColor(color: Int) = apply {
            this.fillColor = color
        }

        fun strokeColor(color: Int) = apply {
            this.strokeColor = color
        }

        fun strokeWidth(width: Int) = apply {
            this.strokeWidth = width
        }

        fun durationBetweenTwoRipples(duration: Long) = apply {
            this.durationBetweenTwoRipples = duration
        }

        fun rippleDuration(duration: Long) = apply {
            this.rippleDuration = duration
        }

        fun build() = RippleMapView(
                context = context,
                googleMap = googleMap,
                latLng = latLng,
                distance = distance,
                transparency = transparency,
                numberOfRipples = numberOfRipples,
                fillColor = fillColor,
                strokeColor = strokeColor,
                strokeWidth = strokeWidth,
                durationBetweenTwoRipples = durationBetweenTwoRipples,
                rippleDuration = rippleDuration
        )
    }
}