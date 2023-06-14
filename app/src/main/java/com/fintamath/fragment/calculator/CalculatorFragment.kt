package com.fintamath.fragment.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
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
    private lateinit var optionsMenu: PopupMenu
    private lateinit var calculatorProcessor: CalculatorProcessor

    private val saveToHistoryDelay: Long = 2000
    private var saveToHistoryTask: TimerTask? = null

    private var isInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        if (!isInitialized) {
            viewBinding = FragmentCalculatorBinding.inflate(inflater, container, false)

            initMathTexts()
            initProcessors()
            initKeyboards()
            initBarButtons()

            isInitialized = true
        }

        if (viewBinding.inTextView.text != CalculatorInputStorage.mathTextData.text) {
            viewBinding.inTextView.text = CalculatorInputStorage.mathTextData.text
        }

        viewBinding.inTextView.requestFocus()

        return viewBinding.root
    }

    override fun onPause() {
        super.onPause()

        runSaveToHistoryTask()
    }

    private fun initMathTexts() {
        viewBinding.inTextLayout.setOnTouchListener { _, event -> touchInText(event) }

        viewBinding.inTextView.text = CalculatorInputStorage.mathTextData.text
        viewBinding.inTextView.setOnTextChangedListener { onInTextChange(it) }
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
        val keyboards = hashMapOf<CalculatorKeyboardType, Pair<KeyboardView, Keyboard>>(
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
        val keyboardSwitcher = CalculatorKeyboardSwitcher(keyboards, currentKeyboardType)

        val listeners = mutableMapOf<CalculatorKeyboardType, CalculatorKeyboardActionListener>()
        for (type in CalculatorKeyboardType.values()) {
            listeners[type] = CalculatorKeyboardActionListener(keyboardSwitcher, viewBinding.inTextView)
        }

        keyboards.forEach { (key: CalculatorKeyboardType, value: Pair<KeyboardView, Keyboard>) ->
            value.first.keyboard = value.second
            value.first.setOnKeyboardActionListener(listeners[key])
        }
    }

    private fun initBarButtons() {
        viewBinding.optionsButton.setOnClickListener { showOptionsMenu() }
        viewBinding.cameraButton.setOnClickListener { showCameraFragment() }
        viewBinding.historyButton.setOnClickListener { showHistoryFragment() }

        optionsMenu = PopupMenu(requireContext(), viewBinding.optionsButton)
        optionsMenu.menuInflater.inflate(R.menu.menu_options, optionsMenu.menu)
        optionsMenu.setOnMenuItemClickListener {
            var result = true;

            when (it.itemId) {
                R.id.settingsButton -> {
                    showSettingsFragment()
                }
                R.id.aboutButton -> {
                    showAboutFragment()
                }
                else -> {
                    result = false;
                }
            }

            return@setOnMenuItemClickListener result
        }
    }

    private fun onInTextChange(text: String) {
        CalculatorInputStorage.mathTextData = MathTextData(text)
        cancelSaveToHistoryTask()

        if (viewBinding.inTextView.text.isEmpty()) {
            viewBinding.solutionView.hideCurrentView()
            calculatorProcessor.stopCurrentCalculations()
        } else if (!viewBinding.inTextView.isComplete) {
            viewBinding.solutionView.showIncompleteInput()
            calculatorProcessor.stopCurrentCalculations()
        } else {
            calculatorProcessor.calculate(text)
        }
    }

    private fun outTexts(texts: List<String>) {
        if (texts.first() == getString(R.string.invalid_input)) {
            viewBinding.solutionView.showInvalidInput()
        } else if (texts.first() == getString(R.string.character_limit_exceeded)) {
            viewBinding.solutionView.showCharacterLimitExceeded()
        } else {
            viewBinding.solutionView.showSolution(texts)

            saveToHistoryTask = Timer().schedule(saveToHistoryDelay) {
                requireActivity().runOnUiThread {
                    onSaveToHistory()
                }
            }
        }
    }

    private fun startLoading() {
        viewBinding.solutionView.showLoading()
    }

    private fun onInTextFocusChange(state: Boolean) {
        if (!state) {
            viewBinding.inTextView.requestFocus()
        }
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
            event.downTime, event.eventTime, event.action, event.x, intTextY, event.metaState
        ))
    }

    private fun onSaveToHistory() {
        HistoryStorage.saveItem(viewBinding.inTextView.text)
        cancelSaveToHistoryTask()
    }

    private fun showOptionsMenu() {
        optionsMenu.show()
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
}
