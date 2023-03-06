package com.fintamath

import androidx.appcompat.app.AppCompatActivity
import com.fintamath.textview.MathEditText
import com.fintamath.textview.MathAlternativesTextView
import com.fintamath.calculator.CalculatorProcessor
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.PopupMenu
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
            KeyboardType.AnalysisKeyboard to
                    Pair(
                        findViewById(R.id.anaysis_keyboard_view),
                        Keyboard(this, R.xml.keyboard_analysis)
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

        val listeners = mutableMapOf<KeyboardType, KeyboardActionListener>()
        for (type in KeyboardType.values()) {
            listeners[type] =
                KeyboardActionListener(calculatorProcessor, keyboardSwitcher, inText)
        }

        keyboards.forEach { (key: KeyboardType, value: Pair<KeyboardView, Keyboard>) ->
            value.first.keyboard = value.second
            value.first.setOnKeyboardActionListener(listeners[key])
        }
    }

    fun showOptionsMenu(view: View) {
        val popup = PopupMenu(this, view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.options_menu, popup.menu)
        popup.show()
    }

    fun showHistoryFragment(view: View) {
        // TODO: implement this
    }

    fun showCameraFragment(view: View) {
        // TODO: implement this
    }
}
