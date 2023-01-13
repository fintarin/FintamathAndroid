package com.fintamath.keyboard_controller

import com.fintamath.calculator.CalculatorProcessor
import com.fintamath.keyboard.KeyboardView
import com.fintamath.textview.MathEditText

class KeyboardActionListener(
    private val calculatorProcessor: CalculatorProcessor,
    private val keyboardSwitcher: KeyboardSwitcher,
    private val inText: MathEditText
) : KeyboardView.OnKeyboardActionListener {

    private val nonSwitchableKeyboardTypes = listOf(
        KeyboardType.MainKeyboard,
        KeyboardType.LogicKeyboard,
    )

    override fun onKey(primaryCode: Int, keyCodes: IntArray) {
        if (primaryCode == ' '.code) {
            return
        }

        val keyCode = KeyboardKeyCode.fromInt(primaryCode)

        if (keyCode == null) {
            inText.insert(primaryCode.toChar().toString())
            switchKeyboardToMain()
            calculatorProcessor.calculate()
            return
        }

        when (keyCode) {
            KeyboardKeyCode.MainKeyboard -> {
                keyboardSwitcher.switchKeyboard(KeyboardType.MainKeyboard)
                return
            }
            KeyboardKeyCode.LettersKeyboard -> {
                keyboardSwitcher.switchKeyboard(KeyboardType.LettersKeyboard)
                return
            }
            KeyboardKeyCode.FunctionsKeyboard -> {
                keyboardSwitcher.switchKeyboard(KeyboardType.FunctionsKeyboard)
                return
            }
            KeyboardKeyCode.LogicKeyboard -> {
                keyboardSwitcher.switchKeyboard(KeyboardType.LogicKeyboard)
                return
            }
            KeyboardKeyCode.History ->                 // TODO
                return
            KeyboardKeyCode.NewLine ->                 // TODO
                return
            KeyboardKeyCode.MoveLeft -> {
                inText.moveCursorLeft()
                return
            }
            KeyboardKeyCode.MoveRight -> {
                inText.moveCursorRight()
                return
            }
            KeyboardKeyCode.Delete -> {
                inText.delete()
                calculatorProcessor.calculate()
                return
            }
            KeyboardKeyCode.Clear -> {
                inText.clear()
                calculatorProcessor.calculate()
                return
            }
            KeyboardKeyCode.Brackets -> {
                inText.insertBrackets()
            }
            KeyboardKeyCode.Log -> {
                inText.insertBinaryFunction(keyCode.toString().lowercase())
            }
            KeyboardKeyCode.Sin,
            KeyboardKeyCode.Cos,
            KeyboardKeyCode.Tan,
            KeyboardKeyCode.Cot,
            KeyboardKeyCode.Asin,
            KeyboardKeyCode.Acos,
            KeyboardKeyCode.Atan,
            KeyboardKeyCode.Acot,
            KeyboardKeyCode.Ln,
            KeyboardKeyCode.Lb,
            KeyboardKeyCode.Lg,
            KeyboardKeyCode.Abs,
            KeyboardKeyCode.Exp,
            KeyboardKeyCode.Sqrt -> {
                inText.insertUnaryFunction(keyCode.toString().lowercase())
            }
            KeyboardKeyCode.Pow2 -> {
                inText.insert("^2")
            }
            KeyboardKeyCode.Pow3 -> {
                inText.insert("^3")
            }
            KeyboardKeyCode.PowN -> {
                inText.insert("^")
            }
            KeyboardKeyCode.Derivative -> {
                inText.insert("'")
            }
            KeyboardKeyCode.Frac -> {
                inText.insertFraction()
            }
            else -> {
            }
        }

        switchKeyboardToMain()
        calculatorProcessor.calculate()
    }

    private fun switchKeyboardToMain() {
        if (!nonSwitchableKeyboardTypes.contains(keyboardSwitcher.currentKeyboardType)) {
            keyboardSwitcher.switchKeyboard(KeyboardType.MainKeyboard)
        }
    }

    override fun onPress(arg0: Int) {}

    override fun onRelease(primaryCode: Int) {}

    override fun onText(text: CharSequence) {}

    override fun swipeDown() {}

    override fun swipeLeft() {}

    override fun swipeRight() {}

    override fun swipeUp() {}
}
