package com.fintamath.widget.loading

import kotlin.jvm.JvmOverloads
import android.widget.LinearLayout
import android.animation.ObjectAnimator
import com.fintamath.R
import android.view.Gravity
import android.view.ViewGroup
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet

class LoadingView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    private val marginMultiplier = 2

    private var dotsRadius = 10
    private var dotsCount = 3
    private var color = Color.parseColor("#FF0000")
    private var animationDuration = 350
    private val maxJump = 3

    private var animator: Array<ObjectAnimator?>? = null

    private var onLayoutReach = false

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LoadingView)

        color = a.getColor(R.styleable.LoadingView_dots_color, Color.parseColor("#FF0000"))
        animationDuration = a.getInt(R.styleable.LoadingView_dots_duration, 10)
        dotsCount = a.getInt(R.styleable.LoadingView_dots_count, dotsCount)
        dotsRadius = a.getDimensionPixelSize(R.styleable.LoadingView_dots_radius, dotsRadius)

        a.recycle()

        adjustView()
        removeAllViews()
        addDots()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        if (!onLayoutReach) {
            onLayoutReach = true
            animateView()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        for (i in 0 until dotsCount) {
            animator?.apply {
                this[i]?.apply {
                    if (isRunning) {
                        removeAllListeners()
                        end()
                        cancel()
                    }
                }
            }
        }
    }

    private fun adjustView() {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        val layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        setLayoutParams(layoutParams)
        setBackgroundColor(Color.TRANSPARENT)
    }

    private fun addDots() {
        val layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        layoutParams.setMargins(
            marginMultiplier * dotsRadius,
            marginMultiplier * dotsRadius,
            marginMultiplier * dotsRadius,
            marginMultiplier * dotsRadius
        )

        for (i in 0 until dotsCount) {
            val circleView = LoadingCircleView(context, dotsRadius, color, true)
            val layout = LinearLayout(context)
            layout.addView(circleView)
            addView(layout, layoutParams)
        }
    }

    private fun animateView() {
        animator = arrayOfNulls(dotsCount)

        for (i in 0 until dotsCount) {
            getChildAt(i).translationY = (height / maxJump).toFloat()

            val y = PropertyValuesHolder.ofFloat(TRANSLATION_Y, (-height / maxJump).toFloat())
            val x = PropertyValuesHolder.ofFloat(TRANSLATION_X, 0f)

            animator!![i] = ObjectAnimator.ofPropertyValuesHolder(getChildAt(i), x, y).apply {
                repeatCount = -1
                repeatMode = ValueAnimator.REVERSE
                duration = animationDuration.toLong()
                startDelay = duration / dotsCount * i
                start()
            }
        }
    }
}
