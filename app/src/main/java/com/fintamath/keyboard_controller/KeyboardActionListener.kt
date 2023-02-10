package com.fintamath.keyboard_controller

import com.fintamath.calculator.CalculatorProcessor
import com.fintamath.keyboard.KeyboardView
import com.fintamath.textview.MathEditText

class KeyboardActionListener(
    private val calculatorProcessor: CalculatorProcessor,
    private val keyboardSwitcher: KeyboardSwitcher,
    private val inText: MathEditText
) : KeyboardView.OnKeyboardActionListener {

    override fun onKey(primaryCode: Int, keyCodes: IntArray) {
        if (primaryCode == ' '.code) {
            return
        }

        val keyCode = KeyboardKeyCode.fromInt(primaryCode)

        if (keyCode == null) {
            inText.insert(primaryCode.toChar().toString())
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
            KeyboardKeyCode.AnalysisKeyboard -> {
                keyboardSwitcher.switchKeyboard(KeyboardType.AnalysisKeyboard)
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
            KeyboardKeyCode.Sinh,
            KeyboardKeyCode.Cosh,
            KeyboardKeyCode.Tanh,
            KeyboardKeyCode.Coth,
            KeyboardKeyCode.Asinh,
            KeyboardKeyCode.Acosh,
            KeyboardKeyCode.Atanh,
            KeyboardKeyCode.Acoth,
            KeyboardKeyCode.Ln,
            KeyboardKeyCode.Lb,
            KeyboardKeyCode.Lg,
            KeyboardKeyCode.Abs,
            KeyboardKeyCode.Exp,
            KeyboardKeyCode.Sqrt,
            KeyboardKeyCode.Sign,
            KeyboardKeyCode.Rad,
            KeyboardKeyCode.F,
            -> {
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
            KeyboardKeyCode.Index1 -> {
                inText.insert("_1")
            }
            KeyboardKeyCode.Index2 -> {
                inText.insert("_2")
            }
            KeyboardKeyCode.IndexN -> {
                inText.insert("_")
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

        calculatorProcessor.calculate()
    }

    override fun onPress(arg0: Int) {}

    override fun onRelease(primaryCode: Int) {}

    override fun onText(text: CharSequence) {}

    override fun swipeDown() {}

    override fun swipeLeft() {}

    override fun swipeRight() {}

    override fun swipeUp() {}
}
