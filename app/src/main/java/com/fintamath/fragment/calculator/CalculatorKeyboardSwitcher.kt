package com.fintamath.fragment.calculator

import android.view.View
import android.widget.ImageButton
import com.fintamath.widget.keyboard.Keyboard
import com.fintamath.widget.keyboard.KeyboardView


internal class CalculatorKeyboardSwitcher(
    private val keyboards: Map<CalculatorKeyboardType, Pair<KeyboardView, Keyboard>>,
    private var currentKeyboardType: CalculatorKeyboardType,
    private var showKeyboardButton: ImageButton
) {

    companion object {
        private const val animationDuration: Long = 250
    }

    private var currentKeyboard: KeyboardView

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
        currentKeyboard.animate()
            .setDuration(animationDuration)
            .alpha(1.0f)
            .translationY(0.0f)

        showKeyboardButton.animate()
            .setDuration(animationDuration)
            .alpha(0.0f)
            .translationY(showKeyboardButton.height.toFloat())
    }

    fun hideCurrentKeyboard() {
        currentKeyboard.animate()
            .setDuration(animationDuration)
            .alpha(0.0f)
            .translationY(currentKeyboard.height.toFloat())

        showKeyboardButton.animate()
            .setDuration(animationDuration)
            .alpha(1.0f)
            .translationY(0.0f)
    }
}
