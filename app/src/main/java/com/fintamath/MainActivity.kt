package com.fintamath

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.fintamath.calculator.CalculatorProcessor
import com.fintamath.fragment.history.HistoryFragment
import com.fintamath.fragment.history.HistoryStorage
import com.fintamath.widget.keyboard.Keyboard
import com.fintamath.widget.keyboard.KeyboardView
import com.fintamath.keyboard.KeyboardActionListener
import com.fintamath.keyboard.KeyboardSwitcher
import com.fintamath.keyboard.KeyboardType
import com.fintamath.widget.mathview.MathSolutionView
import com.fintamath.widget.mathview.MathTextView
import java.io.File
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private lateinit var inTextLayout: ViewGroup
    private lateinit var inTextView: MathTextView
    private lateinit var solutionView: MathSolutionView

    private lateinit var calculatorProcessor: CalculatorProcessor

    private lateinit var historyFragment: HistoryFragment

    private val saveToHistoryDelay: Long = 2000
    private var saveToHistoryTask: TimerTask? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadDataFromFiles()

        inTextLayout = findViewById(R.id.in_text_layout)
        inTextView = findViewById(R.id.in_text)
        solutionView = findViewById(R.id.solution)

        calculatorProcessor = CalculatorProcessor (
            { outTexts(it) },
            { startLoading() }
        )

        initKeyboards()

        initFragments()

        inTextView.setOnTextChangedListener { text -> calculatorProcessor.calculate(text) }
        inTextView.setOnFocusChangeListener { _, state -> callOnInTextFocusChange(state) }

        inTextLayout.setOnTouchListener { _, event -> touchInText(event) }

        inTextView.requestFocus()
    }

    override fun onPause() {
        super.onPause()

        saveToHistoryTask?.cancel()
        saveToHistoryTask?.run()
        saveToHistoryTask = null

        saveDataToFiles()
    }

    private fun loadDataFromFiles() {
        val historyFile = File(applicationContext.filesDir.path + R.string.history_filename)
        historyFile.createNewFile()
        HistoryStorage.loadFromFile(historyFile)
    }

    private fun saveDataToFiles() {
        val historyFile = File(applicationContext.filesDir.path + R.string.history_filename)
        historyFile.createNewFile()
        HistoryStorage.saveToFile(historyFile)
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
                        findViewById(R.id.analysis_keyboard_view),
                        Keyboard(this, R.xml.keyboard_analysis)
                    ),
            KeyboardType.LogicKeyboard to
                    Pair(
                        findViewById(R.id.logic_keyboard_view),
                        Keyboard(this, R.xml.keyboard_logic)
                    )
        )

        val currentKeyboardType = KeyboardType.values().first()
        val keyboardSwitcher = KeyboardSwitcher(keyboards, currentKeyboardType)

        val listeners = mutableMapOf<KeyboardType, KeyboardActionListener>()
        for (type in KeyboardType.values()) {
            listeners[type] = KeyboardActionListener(keyboardSwitcher, inTextView)
        }

        keyboards.forEach { (key: KeyboardType, value: Pair<KeyboardView, Keyboard>) ->
            value.first.keyboard = value.second
            value.first.setOnKeyboardActionListener(listeners[key])
        }
    }

    private fun initFragments() {
        historyFragment = HistoryFragment()
        historyFragment.onCalculate = { inTextView.text = it }
    }

    private fun outTexts(it: List<String>) {
        runOnUiThread {
            saveToHistoryTask?.cancel()
            saveToHistoryTask = null

            if (!inTextView.isComplete) {
                solutionView.showIncompleteInput()
            } else if (it.isEmpty() || it.first() == "") {
                solutionView.hideCurrentView()
            } else if (it.first() == getString(R.string.invalid_input)) {
                solutionView.showInvalidInput()
            } else {
                solutionView.showSolution(it)
                saveToHistoryTask = Timer().schedule(saveToHistoryDelay) { callOnSaveToHistory() }
            }
        }
    }

    private fun startLoading() {
        runOnUiThread {
            solutionView.showLoading()
        }
    }

    private fun touchInText(event: MotionEvent): Boolean {
        return inTextView.dispatchTouchEvent(MotionEvent.obtain(
            event.downTime, event.eventTime, event.action, event.x, 0f, event.metaState
        ))
    }

    private fun callOnInTextFocusChange(state: Boolean) {
        if (!state) {
            inTextView.requestFocus()
        }
    }

    private fun callOnSaveToHistory() {
        runOnUiThread {
            HistoryStorage.saveItem(inTextView.text)
            saveToHistoryTask?.cancel()
            saveToHistoryTask = null
        }
    }

    fun showOptionsMenu(view: View) {
        val popup = PopupMenu(this, view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.options_menu, popup.menu)
        popup.show()
    }

    fun showHistoryFragment(view: View) {
        saveToHistoryTask?.cancel()
        saveToHistoryTask?.run()
        saveToHistoryTask = null

        inTextView.clearFocus()

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(android.R.id.content, historyFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun showCameraFragment(view: View) {
        // TODO: implement this
    }
}
 