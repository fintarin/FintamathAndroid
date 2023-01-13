package com.fintamath

import androidx.appcompat.app.AppCompatActivity
import com.fintamath.textview.MathEditText
import com.fintamath.textview.MathAlternativesTextView
import com.fintamath.calculator.CalculatorProcessor
import android.os.Bundle
import android.view.View
import com.fintamath.keyboard.Keyboard
import com.fintamath.keyboard.KeyboardView
import com.fintamath.keyboard_controller.KeyboardType
import com.fintamath.keyboard_controller.KeyboardSwitcher
import com.fintamath.keyboard_controller.KeyboardActionListener

class MainActivity : AppCompatActivity() {
    private lateinit var inText: MathEditText
    private lateinit var outText: MathAlternativesTextView
    private lateinit var currentKeyboard: KeyboardView
    private lateinit var calculatorProcessor: CalculatorProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inText = findViewById(R.id.inText)
        outText = findViewById(R.id.outText)
        calculatorProcessor = CalculatorProcessor(this, inText, outText)

        initKeyboards()
        currentKeyboard.visibility = View.VISIBLE

        inText.requestFocus()
    }

    private fun initKeyboards() {
        val keyboards = hashMapOf<KeyboardType, Pair<KeyboardView, Keyboard>>(
            KeyboardType.MainKeyboard to
                    Pair(
                        findViewById(R.id.main_keyboard_view),
                        Keyboard(this, R.xml.keyboard_main)
                    ),
            KeyboardType.LettersKeyboard to
                    Pair(
                        findViewById(R.id.letters_keyboard_view),
                        Keyboard(this, R.xml.keyboard_letters)
                    ),
            KeyboardType.FunctionsKeyboard to
                    Pair(
                        findViewById(R.id.functions_keyboard_view),
                        Keyboard(this, R.xml.keyboard_functions)
                    ),
            KeyboardType.LogicKeyboard to
                    Pair(
                        findViewById(R.id.logic_keyboard_view),
                        Keyboard(this, R.xml.keyboard_logic)
                    )
        )

        val currentKeyboardType = KeyboardType.values().first()
        currentKeyboard = keyboards[currentKeyboardType]!!.first
        val keyboardSwitcher = KeyboardSwitcher(keyboards, currentKeyboardType)

        val listeners = hashMapOf(
            KeyboardType.MainKeyboard to
                    KeyboardActionListener(calculatorProcessor, keyboardSwitcher, inText),
            KeyboardType.LettersKeyboard to
                    KeyboardActionListener(calculatorProcessor, keyboardSwitcher, inText),
            KeyboardType.FunctionsKeyboard to
                    KeyboardActionListener(calculatorProcessor, keyboardSwitcher, inText),
            KeyboardType.LogicKeyboard to
                    KeyboardActionListener(calculatorProcessor, keyboardSwitcher, inText)
        )

        keyboards.forEach { (key: KeyboardType, value: Pair<KeyboardView, Keyboard>) ->
            value.first.keyboard = value.second
            value.first.setOnKeyboardActionListener(listeners[key])
        }
    }
}
