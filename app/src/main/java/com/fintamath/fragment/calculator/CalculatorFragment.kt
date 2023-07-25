package com.fintamath.fragment.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.fintamath.R
import com.fintamath.calculator.CalculatorProcessor
import com.fintamath.databinding.FragmentCalculatorBinding
import com.fintamath.storage.HistoryStorage
import com.fintamath.storage.MathTextData
import com.fintamath.storage.CalculatorInputStorage
import com.fintamath.widget.keyboard.Keyboard
import com.fintamath.widget.keyboard.KeyboardView
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule


class CalculatorFragment : Fragment() {

    private lateinit var viewBinding: FragmentCalculatorBinding
    private lateinit var calculatorProcessor: CalculatorProcessor
    private lateinit var keyboardSwitcher: CalculatorKeyboardSwitcher

    private val saveToHistoryDelay: Long = 2000
    private var saveToHistoryTask: TimerTask? = null

    private val requestFocusDelay: Long = 50

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
        }

        return viewBinding.root
    }

    override fun onResume() {
        super.onResume()

        if (viewBinding.inTextView.text != CalculatorInputStorage.mathTextData.text) {
            viewBinding.inTextView.text = CalculatorInputStorage.mathTextData.text
        }

        if (viewBinding.outSolutionView.isShowingLoading()) {
            calculatorProcessor.calculate(viewBinding.inTextView.text)
        }

        viewBinding.inTextView.requestFocus()
        keyboardSwitcher.showCurrentKeyboard()
    }

    override fun onPause() {
        super.onPause()

        calculatorProcessor.stopCurrentCalculations()
        runSaveToHistoryTask()
    }

    private fun initMathTexts() {
        viewBinding.inTextLayout.setOnTouchListener { _, event -> touchInText(event) }

        viewBinding.inTextView.text = CalculatorInputStorage.mathTextData.text
        viewBinding.inTextView.setOnTextChangedListener { onInTextChange(it) }
        viewBinding.inTextView.setOnFocusChangeListener { _, state -> onInTextFocusChange(state) }

        Timer().schedule(requestFocusDelay) {
            requireActivity().runOnUiThread {
                viewBinding.inTextView.requestFocus()
            }
        }
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
            // TODO: uncomment when derivatives and integral functions will be better implemented
            // CalculatorKeyboardType.AnalysisKeyboard to
            //         Pair(
            //             viewBinding.analysisKeyboardView.root,
            //             Keyboard(requireContext(), R.xml.keyboard_analysis)
            //         ),
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
            viewBinding.inTextView.requestFocus()
        }
        viewBinding.outSolutionView.setOnClickListener {
            keyboardSwitcher.hideCurrentKeyboard()
            viewBinding.inTextView.clearFocus()
        }
        viewBinding.inOutLayout.setOnClickListener {
            keyboardSwitcher.hideCurrentKeyboard()
            viewBinding.inTextView.clearFocus()
        }
    }

    private fun initBarButtons() {
        viewBinding.cameraButton.setOnClickListener { showCameraFragment() }
        viewBinding.historyButton.setOnClickListener { showHistoryFragment() }
        viewBinding.settingsButton.setOnClickListener { showSettingsFragment() }
        viewBinding.aboutButton.setOnClickListener { showAboutFragment() }
    }

    private fun onInTextChange(text: String) {
        CalculatorInputStorage.mathTextData = MathTextData(text)
        cancelSaveToHistoryTask()

        if (viewBinding.inTextView.text.isEmpty()) {
            calculatorProcessor.stopCurrentCalculations()
            viewBinding.outSolutionView.hideCurrentView()
        } else if (!viewBinding.inTextView.isComplete) {
            calculatorProcessor.stopCurrentCalculations()
            viewBinding.outSolutionView.showIncompleteInput()
        } else {
            calculatorProcessor.calculate(text)
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
            viewBinding.outSolutionView.showSolution(cutSolutionTexts(texts))

            saveToHistoryTask = Timer().schedule(saveToHistoryDelay) {
                requireActivity().runOnUiThread {
                    onSaveToHistory()
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

    private fun showFragment(navigationId: Int) {
        runSaveToHistoryTask()
        viewBinding.inTextView.clearFocus()
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
        val result = mutableListOf<String>()
        val distinctTexts = texts.distinct()

        for (i in distinctTexts.indices) {
            if (removeSpacesAndBrackets(distinctTexts[i]) != removeSpacesAndBrackets(viewBinding.inTextView.text)) {
                result.add(distinctTexts[i])
            }
        }

        if (result.isEmpty()) {
            result.add(distinctTexts.last())
        }

        return result
    }

    private fun removeSpacesAndBrackets(text: String): String {
        var result = text
        result = result.replace("(", "");
        result = result.replace(")", "");
        result = result.replace(" ", "");
        return result
    }
}
