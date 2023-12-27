package com.fintamath.fragment.calculator

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.fintamath.R
import com.fintamath.calculator.CalculatorProcessor
import com.fintamath.databinding.FragmentCalculatorBinding
import com.fintamath.storage.HistoryStorage
import com.fintamath.storage.CalculatorStorage
import com.fintamath.storage.MathTextData
import com.fintamath.storage.SettingsStorage
import com.fintamath.widget.keyboard.Keyboard
import com.fintamath.widget.keyboard.KeyboardView
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.math.BigDecimal
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.schedule


class CalculatorFragment : Fragment() {

    private enum class InTextViewState(val value: Int) {
        Created(0),
        Changed(1),
        Ready(2),
    }

    private lateinit var viewBinding: FragmentCalculatorBinding
    private lateinit var calculatorProcessor: CalculatorProcessor
    private lateinit var keyboardSwitcher: CalculatorKeyboardSwitcher

    private val saveToHistoryDelay: Long = 2000
    private var saveToHistoryTask: TimerTask? = null

    private var wereSettingsUpdated = AtomicBoolean(false)

    private var drawGraphJob: Job? = null

    private val maxSolutionLength = 2000

    private val inTextViewPreloadString = "abc * 123 Pi E I Inf ComplexInf () sqrt() abs() floor() ceil() derivative(x,x)"
    private var inTextViewInitialColor = 0

    private var inTextViewState = AtomicInteger(InTextViewState.Created.value)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        if (!this::viewBinding.isInitialized) {
            viewBinding = FragmentCalculatorBinding.inflate(inflater, container, false)

            initMathTexts()
            initProcessors()
            initKeyboards()
            initKeyboardActions()
            initBarButtons()

            // Preloading - part 1
            inTextViewInitialColor = viewBinding.inTextView.textColor
            viewBinding.inTextView.textColor = (viewBinding.inOutLayout.background as ColorDrawable).color
            viewBinding.inTextView.text = inTextViewPreloadString
        }

        return viewBinding.root
    }

    override fun onResume() {
        super.onResume()

        keyboardSwitcher.showCurrentKeyboard()

        updateSettings()

        if (inTextViewState.get() == InTextViewState.Ready.value) {
            if (viewBinding.inTextView.text != CalculatorStorage.inputMathTextData.text) {
                viewBinding.inTextView.text = CalculatorStorage.inputMathTextData.text
            }

            if (viewBinding.outSolutionView.isShowingLoading() || wereSettingsUpdated.get()) {
                startLoading()
                onInTextChange(viewBinding.inTextView.text)
            }

            viewBinding.inTextView.requestFocusInWeb()
        }

        wereSettingsUpdated.set(false)
    }

    override fun onPause() {
        super.onPause()

        calculatorProcessor.stopCurrentCalculations()
        runSaveToHistoryTask()
    }

    private fun initMathTexts() {
        viewBinding.inTextLayout.setOnTouchListener { _, event -> touchInText(event) }

        viewBinding.inTextView.text = CalculatorStorage.inputMathTextData.text
        viewBinding.inTextView.setOnTextChangedListener { _, text -> onInTextChange(text) }
        viewBinding.inTextView.setOnFocusChangeListener { _, state -> onInTextFocusChange(state) }
    }

    private fun initProcessors() {
        calculatorProcessor = CalculatorProcessor (
            { requireActivity().runOnUiThread { it.invoke() } },
            { outTexts(it) },
            { startLoading() }
        )
    }

    private fun initKeyboards() {
        val keyboards = hashMapOf(
            CalculatorKeyboardType.MainKeyboard to
                    Pair(
                        viewBinding.mainKeyboardView.root,
                        Keyboard(requireContext(), R.xml.keyboard_main)
                    ),
            CalculatorKeyboardType.LettersKeyboard to
                    Pair(
                        viewBinding.lettersKeyboardView.root,
                        Keyboard(requireContext(), R.xml.keyboard_letters)
                    ),
            CalculatorKeyboardType.FunctionsKeyboard to
                    Pair(
                        viewBinding.functionsKeyboardView.root,
                        Keyboard(requireContext(), R.xml.keyboard_functions)
                    ),
            CalculatorKeyboardType.AnalysisKeyboard to
                    Pair(
                        viewBinding.analysisKeyboardView.root,
                        Keyboard(requireContext(), R.xml.keyboard_analysis)
                    ),
            CalculatorKeyboardType.LogicKeyboard to
                    Pair(
                        viewBinding.logicKeyboardView.root,
                        Keyboard(requireContext(), R.xml.keyboard_logic)
                    )
        )

        val currentKeyboardType = CalculatorKeyboardType.values().first()
        keyboardSwitcher = CalculatorKeyboardSwitcher(keyboards, currentKeyboardType)

        val listeners = mutableMapOf<CalculatorKeyboardType, CalculatorKeyboardActionListener>()
        for (type in CalculatorKeyboardType.values()) {
            listeners[type] = CalculatorKeyboardActionListener(keyboardSwitcher, viewBinding.inTextView)
        }

        keyboards.forEach { (key: CalculatorKeyboardType, value: Pair<KeyboardView, Keyboard>) ->
            value.first.keyboard = value.second
            value.first.setOnKeyboardActionListener(listeners[key])
        }
    }

    private fun initKeyboardActions() {
        viewBinding.inTextView.setOnClickListener {
            keyboardSwitcher.showCurrentKeyboard()
        }
        viewBinding.outSolutionView.setOnClickListener {
            keyboardSwitcher.hideCurrentKeyboard()
            viewBinding.inTextView.clearFocusInWeb()
        }
        viewBinding.inOutLayout.setOnClickListener {
            keyboardSwitcher.hideCurrentKeyboard()
            viewBinding.inTextView.clearFocusInWeb()
        }
    }

    private fun initBarButtons() {
        // viewBinding.cameraButton.setOnClickListener { showCameraFragment() } // TODO: uncomment when camera is implemented
        viewBinding.historyButton.setOnClickListener { showHistoryFragment() }
        viewBinding.settingsButton.setOnClickListener { showSettingsFragment() }
        viewBinding.aboutButton.setOnClickListener { showAboutFragment() }
        viewBinding.graphButton.setOnClickListener { showGraphFragment() }
    }

    private fun updateSettings() {
        val precision = SettingsStorage.getPrecision()
        if (precision != calculatorProcessor.getPrecision()) {
            calculatorProcessor.setPrecision(precision)
            wereSettingsUpdated.set(true)
        }
    }

    private fun onInTextChange(text: String) {
        when (inTextViewState.get()) {
            InTextViewState.Created.value -> {
                // Preloading - part 2
                viewBinding.inTextView.clear()
                inTextViewState.incrementAndGet()
                return
            }
            InTextViewState.Changed.value -> {
                // Preloading - part 3
                viewBinding.inTextView.clearUndoStates()
                viewBinding.inTextView.requestFocusInWeb()
                viewBinding.inTextView.textColor = inTextViewInitialColor
                inTextViewState.incrementAndGet()
                return
            }
        }

        CalculatorStorage.inputMathTextData = MathTextData(text)
        cancelSaveToHistoryTask()

        viewBinding.inTextViewHint.visibility = if (viewBinding.inTextView.text.isNotEmpty())
            View.GONE else
            View.VISIBLE

        viewBinding.outSolutionView.visibility = if (viewBinding.inTextView.text.isEmpty())
            View.GONE else
            View.VISIBLE

        if (viewBinding.inTextView.text.isEmpty()) {
            calculatorProcessor.stopCurrentCalculations()
            viewBinding.outSolutionView.hideCurrentView()
        } else if (!viewBinding.inTextView.isComplete) {
            calculatorProcessor.stopCurrentCalculations()
            viewBinding.outSolutionView.showIncompleteInput()
        } else {
            val formattedText = text.replace('[', '(').replace(']', ')')
            calculatorProcessor.calculate(formattedText)
        }
    }

    private fun onInTextFocusChange(state: Boolean) {
        if (state) {
            keyboardSwitcher.showCurrentKeyboard()
        }
    }

    private fun outTexts(texts: List<String>) {
        if (texts.first() == getString(R.string.invalid_input)) {
            viewBinding.outSolutionView.showInvalidInput()
        } else if (texts.first() == getString(R.string.character_limit_exceeded)) {
            viewBinding.outSolutionView.showCharacterLimitExceeded()
        } else if (texts.first() == getString(R.string.failed_to_solve)) {
            // TODO: send a bug report here
            viewBinding.outSolutionView.showFailedToSolve()
        } else {
            val cutSolutionTexts = cutSolutionTexts(texts)
            val firstSolutionText = cutSolutionTexts.first()
            CalculatorStorage.outputMathTextData.text = firstSolutionText

            if (countTextsLength(cutSolutionTexts) > maxSolutionLength) {
                viewBinding.outSolutionView.showCharacterLimitExceeded()
            } else {
                viewBinding.outSolutionView.showSolution(cutSolutionTexts)

                saveToHistoryTask = Timer().schedule(saveToHistoryDelay) {
                    requireActivity().runOnUiThread {
                        onSaveToHistory()
                    }
                }
            }
        }
    }

    private fun startLoading() {
        viewBinding.outSolutionView.showLoading()
    }

    private fun touchInText(event: MotionEvent): Boolean {
        val inTextLayoutLocation = IntArray(2)
        viewBinding.inTextLayout.getLocationOnScreen(inTextLayoutLocation)

        val inTextViewLocation = IntArray(2)
        viewBinding.inTextView.getLocationOnScreen(inTextViewLocation)

        val heightDelta = (inTextViewLocation[1] - inTextLayoutLocation[1]).toFloat() / 4

        var intTextY = event.y + inTextLayoutLocation[1]

        if (intTextY < inTextViewLocation[1]) {
            intTextY = heightDelta
        } else if (intTextY >= inTextViewLocation[1] + viewBinding.inTextView.height) {
            intTextY = viewBinding.inTextView.height - heightDelta
        } else {
            intTextY -= inTextViewLocation[1]
        }

        return viewBinding.inTextView.onTouchEvent(MotionEvent.obtain(
            event.downTime,
            event.eventTime,
            event.action,
            event.x,
            intTextY,
            event.metaState
        ))
    }

    private fun onSaveToHistory() {
        HistoryStorage.saveItem(viewBinding.inTextView.text)
        cancelSaveToHistoryTask()
    }

    private fun showHistoryFragment() {
        showFragment(R.id.action_calculatorFragment_to_historyFragment)
    }

    private fun showCameraFragment() {
        showFragment(R.id.action_calculatorFragment_to_cameraFragment)
    }

    private fun showAboutFragment() {
        showFragment(R.id.action_calculatorFragment_to_aboutFragment)
    }

    private fun showSettingsFragment() {
        showFragment(R.id.action_calculatorFragment_to_settingsFragment)
    }

    private fun showGraphFragment() {
        showFragment(R.id.action_calculatorFragment_to_graphFragment)
    }

    private fun showFragment(navigationId: Int) {
        runSaveToHistoryTask()
        viewBinding.inTextView.clearFocusInWeb()
        viewBinding.root.findNavController().navigate(navigationId)
    }

    private fun runSaveToHistoryTask() {
        saveToHistoryTask?.cancel()
        saveToHistoryTask?.run()
        saveToHistoryTask = null
    }

    private fun cancelSaveToHistoryTask() {
        saveToHistoryTask?.cancel()
        saveToHistoryTask = null
    }

    private fun cutSolutionTexts(texts: List<String>): List<String> {
        val inText = texts.first()
        val solTexts = texts.distinct().toMutableList()

        if (solTexts.size > 1 && solTexts.first() == inText) {
            solTexts.removeFirst()
        }

        while (solTexts.size > 1 && countTextsLength(solTexts) > maxSolutionLength) {
            solTexts.removeFirst()
        }

        return solTexts
    }

    private fun countTextsLength(distinctTexts: List<String>): Int {
        var resultLength = 0
        distinctTexts.forEach { resultLength += it.length }
        return resultLength
    }
}
