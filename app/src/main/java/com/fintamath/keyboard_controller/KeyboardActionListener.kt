package com.fintamath.keyboard_controller

import com.fintamath.calculator.CalculatorProcessor
import com.fintamath.keyboard.KeyboardView
import com.fintamath.textview.MathTextView

class KeyboardActionListener(
    private val calculatorProcessor: CalculatorProcessor,
    private val keyboardSwitcher: KeyboardSwitcher,
    private val inText: MathTextView
) : KeyboardView.OnKeyboardActionListener {

    init {
        inText.setOnTextChangedListener {
            calculatorProcessor.calculate(it)
        }
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray) {
        val keyCode = KeyboardKeyCode.fromInt(primaryCode) ?: return

        when (keyCode) {
            KeyboardKeyCode.MainKeyboard -> {
                keyboardSwitcher.switchKeyboard(KeyboardType.MainKeyboard)
            }
            KeyboardKeyCode.LettersKeyboard -> {
                keyboardSwitcher.switchKeyboard(KeyboardType.LettersKeyboard)
            }
            KeyboardKeyCode.FunctionsKeyboard -> {
                keyboardSwitcher.switchKeyboard(KeyboardType.FunctionsKeyboard)
            }
            KeyboardKeyCode.AnalysisKeyboard -> {
                keyboardSwitcher.switchKeyboard(KeyboardType.AnalysisKeyboard)
            }
            KeyboardKeyCode.LogicKeyboard -> {
                keyboardSwitcher.switchKeyboard(KeyboardType.LogicKeyboard)
            }
            KeyboardKeyCode.MoveLeft -> {
                inText.moveCursorLeft()
            }
            KeyboardKeyCode.MoveRight -> {
                inText.moveCursorRight()
            }
            KeyboardKeyCode.Delete -> {
                inText.deleteAtCursor()
            }
            KeyboardKeyCode.Clear -> {
                inText.clear()
            }
            KeyboardKeyCode.Undo -> {
                inText.undo()
            }
            KeyboardKeyCode.Redo -> {
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
