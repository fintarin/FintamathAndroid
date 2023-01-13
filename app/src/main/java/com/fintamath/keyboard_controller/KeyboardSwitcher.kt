package com.fintamath.keyboard_controller

import android.view.View
import com.fintamath.keyboard.Keyboard
import com.fintamath.keyboard.KeyboardView

class KeyboardSwitcher(
    private val keyboards: Map<KeyboardType, Pair<KeyboardView, Keyboard>>,
    var currentKeyboardType: KeyboardType
) {

    private var currentKeyboard: KeyboardView

    init {
        currentKeyboard = keyboards[currentKeyboardType]!!.first
    }

    fun switchKeyboard(keyboardType: KeyboardType) {
        if (currentKeyboardType === keyboardType) {
            return
        }

        currentKeyboard.visibility = View.GONE
        currentKeyboard = keyboards[keyboardType]!!.first
        currentKeyboard.visibility = View.VISIBLE
        currentKeyboardType = keyboardType
    }
}
