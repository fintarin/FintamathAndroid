package com.fintamath.fragment.calculator

import android.animation.Animator
import android.view.View
import android.widget.ImageButton
import com.fintamath.widget.keyboard.Keyboard
import com.fintamath.widget.keyboard.KeyboardView
import java.util.concurrent.atomic.AtomicBoolean


internal class CalculatorKeyboardSwitcher(
    private val keyboards: Map<CalculatorKeyboardType, Pair<KeyboardView, Keyboard>>,
    private var currentKeyboardType: CalculatorKeyboardType,
) {

    companion object {
        private const val animationDuration: Long = 300
    }

    private var currentKeyboard: KeyboardView
    private var isHiding: AtomicBoolean = AtomicBoolean(false)

    init {
        currentKeyboard = keyboards[currentKeyboardType]!!.first
    }

    fun switchKeyboard(keyboardType: CalculatorKeyboardType) {
        if (currentKeyboardType == keyboardType) {
            return
        }

        currentKeyboard.visibility = View.GONE
        currentKeyboard = keyboards[keyboardType]!!.first
        currentKeyboard.visibility = View.VISIBLE
        currentKeyboardType = keyboardType
    }

    fun showCurrentKeyboard() {
        isHiding.set(false)

        currentKeyboard.visibility = View.VISIBLE

        currentKeyboard.animate()
            .setDuration(animationDuration)
            .alpha(1.0f)
            .translationY(0.0f)
            .setListener(null)
    }

    fun hideCurrentKeyboard() {
        isHiding.set(true)

        currentKeyboard.animate()
            .setDuration(animationDuration)
            .alpha(0.0f)
            .translationY(currentKeyboard.height.toFloat())
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) = Unit

                override fun onAnimationEnd(animation: Animator) {
                    if (isHiding.get()) {
                        currentKeyboard.visibility = View.GONE

                        isHiding.set(false)
                    }
                }

                override fun onAnimationCancel(animation: Animator)  = Unit

                override fun onAnimationRepeat(animation: Animator)  = Unit
            })
    }
}
