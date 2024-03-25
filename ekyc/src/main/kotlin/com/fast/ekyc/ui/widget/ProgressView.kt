package com.fast.ekyc.ui.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.fast.ekyc.R
import kotlin.math.floor

internal class ProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    companion object {
        private const val DURATION = 800L
        private const val CIRCLE_COUNT = 8
    }

    private var circularAnimation: Animation? = null

    init {
        ContextCompat.getDrawable(context, R.drawable.kyc_ic_progress)?.also {
            DrawableCompat.setTint(it, Color.WHITE)
            setImageDrawable(it)
            setAnimation()
        }
    }

    private fun setAnimation() {
        AnimationUtils.loadAnimation(context, R.anim.kyc_anim_circular_progress)?.also {
            circularAnimation = it
            it.duration = DURATION
            it.interpolator =
                Interpolator { input -> floor((input * CIRCLE_COUNT).toDouble()).toFloat() / CIRCLE_COUNT }
            startAnimation(circularAnimation)
        }
    }

    fun stopAnimation() {
        clearAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearAnimation()
    }
}