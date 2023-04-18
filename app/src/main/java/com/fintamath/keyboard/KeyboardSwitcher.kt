package com.fintamath.keyboard

import android.view.View
import com.fintamath.widget.keyboard.Keyboard
import com.fintamath.widget.keyboard.KeyboardView

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
