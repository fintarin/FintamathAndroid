package com.fintamath.fragment.calculator

import android.view.View
import com.fintamath.widget.keyboard.Keyboard
import com.fintamath.widget.keyboard.KeyboardView

internal class CalculatorKeyboardSwitcher(
    private val keyboards: Map<CalculatorKeyboardType, Pair<KeyboardView, Keyboard>>,
    var currentKeyboardType: CalculatorKeyboardType
) {

    private var currentKeyboard: KeyboardView

    init {
        currentKeyboard = keyboards[currentKeyboardType]!!.first
    }

    fun switchKeyboard(keyboardType: CalculatorKeyboardType) {
        if (currentKeyboardType === keyboardType) {
            return
        }

        currentKeyboard.visibility = View.GONE
        currentKeyboard = keyboards[keyboardType]!!.first
        currentKeyboard.visibility = View.VISIBLE
        currentKeyboardType = keyboardType
    }
}
