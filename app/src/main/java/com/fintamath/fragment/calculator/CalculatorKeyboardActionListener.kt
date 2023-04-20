package com.fintamath.fragment.calculator

import com.fintamath.widget.keyboard.KeyboardView
import com.fintamath.widget.mathview.MathTextView

internal class CalculatorKeyboardActionListener(
    private val keyboardSwitcher: CalculatorKeyboardSwitcher,
    private val inText: MathTextView
) : KeyboardView.OnKeyboardActionListener {

    override fun onKey(primaryCode: Int, keyCodes: IntArray) {
        val keyCode = CalculatorKeyboardKeyCode.fromInt(primaryCode) ?: return

        when (keyCode) {
            CalculatorKeyboardKeyCode.MainKeyboard -> {
                keyboardSwitcher.switchKeyboard(CalculatorKeyboardType.MainKeyboard)
            }
            CalculatorKeyboardKeyCode.LettersKeyboard -> {
                keyboardSwitcher.switchKeyboard(CalculatorKeyboardType.LettersKeyboard)
            }
            CalculatorKeyboardKeyCode.FunctionsKeyboard -> {
                keyboardSwitcher.switchKeyboard(CalculatorKeyboardType.FunctionsKeyboard)
            }
            CalculatorKeyboardKeyCode.AnalysisKeyboard -> {
                keyboardSwitcher.switchKeyboard(CalculatorKeyboardType.AnalysisKeyboard)
            }
            CalculatorKeyboardKeyCode.LogicKeyboard -> {
                keyboardSwitcher.switchKeyboard(CalculatorKeyboardType.LogicKeyboard)
            }
            CalculatorKeyboardKeyCode.MoveLeft -> {
                inText.moveCursorLeft()
            }
            CalculatorKeyboardKeyCode.MoveRight -> {
                inText.moveCursorRight()
            }
            CalculatorKeyboardKeyCode.Delete -> {
                inText.deleteAtCursor()
            }
            CalculatorKeyboardKeyCode.Clear -> {
                inText.clear()
            }
            CalculatorKeyboardKeyCode.Undo -> {
                inText.undo()
            }
            CalculatorKeyboardKeyCode.Redo -> {
                inText.redo()
            }
        }
    }

    override fun onPress(arg0: Int) {}

    override fun onRelease(primaryCode: Int) {}

    override fun onText(text: CharSequence) {
        inText.insertAtCursor(text.toString())
    }

    override fun swipeDown() {}

    override fun swipeLeft() {}

    override fun swipeRight() {}

    override fun swipeUp() {}
}
