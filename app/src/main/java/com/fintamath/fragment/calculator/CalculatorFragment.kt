package com.fintamath.fragment.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.fintamath.R
import com.fintamath.calculator.CalculatorProcessor
import com.fintamath.storage.HistoryStorage
import com.fintamath.storage.MathTextData
import com.fintamath.storage.CalculatorInputStorage
import com.fintamath.widget.keyboard.Keyboard
import com.fintamath.widget.keyboard.KeyboardView
import com.fintamath.widget.mathview.MathSolutionView
import com.fintamath.widget.mathview.MathTextView
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule


class CalculatorFragment : Fragment() {

    private lateinit var inTextLayout: ViewGroup
    private lateinit var inTextView: MathTextView
    private lateinit var solutionView: MathSolutionView
    private lateinit var optionsMenu: PopupMenu
    private lateinit var calculatorProcessor: CalculatorProcessor
    private var fragmentView: View? = null

    private val saveToHistoryDelay: Long = 2000
    private var saveToHistoryTask: TimerTask? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        if (fragmentView == null) {
            fragmentView = inflater.inflate(R.layout.fragment_calculator, container, false)

            initMathTexts(fragmentView!!)
            initProcessors()
            initKeyboards(fragmentView!!)
            initBarButtons(fragmentView!!)
        }

        if (inTextView.text != CalculatorInputStorage.mathTextData.text) {
            inTextView.text = CalculatorInputStorage.mathTextData.text
        }

        inTextView.requestFocus()

        return fragmentView!!
    }

    override fun onPause() {
        super.onPause()

        runSaveToHistoryTask()
    }

    private fun initMathTexts(fragmentView: View) {
        inTextLayout = fragmentView.findViewById(R.id.inMathTextLayout)
        inTextLayout.setOnTouchListener { _, event -> touchInText(event) }

        inTextView = fragmentView.findViewById(R.id.inText)
        inTextView.text = CalculatorInputStorage.mathTextData.text
        inTextView.setOnTextChangedListener { callOnInTextChange(it) }
        inTextView.setOnFocusChangeListener { _, state -> callOnInTextFocusChange(state) }

        solutionView = fragmentView.findViewById(R.id.solution)
    }

    private fun initProcessors() {
        calculatorProcessor = CalculatorProcessor (
            { requireActivity().runOnUiThread { it.invoke() } },
            { outTexts(it) },
            { startLoading() }
        )
    }

    private fun initKeyboards(fragmentView: View) {
        val keyboards = hashMapOf<CalculatorKeyboardType, Pair<KeyboardView, Keyboard>>(
            CalculatorKeyboardType.MainKeyboard to
                    Pair(
                        fragmentView.findViewById(R.id.mainKeyboardView),
                        Keyboard(requireContext(), R.xml.keyboard_main)
                    ),
            CalculatorKeyboardType.LettersKeyboard to
                    Pair(
                        fragmentView.findViewById(R.id.lettersKeyboardView),
                        Keyboard(requireContext(), R.xml.keyboard_letters)
                    ),
            CalculatorKeyboardType.FunctionsKeyboard to
                    Pair(
                        fragmentView.findViewById(R.id.functionsKeyboardView),
                        Keyboard(requireContext(), R.xml.keyboard_functions)
                    ),
            CalculatorKeyboardType.AnalysisKeyboard to
                    Pair(
                        fragmentView.findViewById(R.id.analysisKeyboardView),
                        Keyboard(requireContext(), R.xml.keyboard_analysis)
                    ),
            CalculatorKeyboardType.LogicKeyboard to
                    Pair(
                        fragmentView.findViewById(R.id.logicKeyboardView),
                        Keyboard(requireContext(), R.xml.keyboard_logic)
                    )
        )

        val currentKeyboardType = CalculatorKeyboardType.values().first()
        val keyboardSwitcher = CalculatorKeyboardSwitcher(keyboards, currentKeyboardType)

        val listeners = mutableMapOf<CalculatorKeyboardType, CalculatorKeyboardActionListener>()
        for (type in CalculatorKeyboardType.values()) {
            listeners[type] = CalculatorKeyboardActionListener(keyboardSwitcher, inTextView)
        }

        keyboards.forEach { (key: CalculatorKeyboardType, value: Pair<KeyboardView, Keyboard>) ->
            value.first.keyboard = value.second
            value.first.setOnKeyboardActionListener(listeners[key])
        }
    }

    private fun initBarButtons(fragmentView: View) {
        val optionsButton: ImageButton = fragmentView.findViewById(R.id.optionsButton)
        optionsButton.setOnClickListener { showOptionsMenu(fragmentView) }

        val cameraButton: ImageButton = fragmentView.findViewById(R.id.cameraButton)
        cameraButton.setOnClickListener { showCameraFragment(fragmentView) }

        val historyButton: ImageButton = fragmentView.findViewById(R.id.historyButton)
        historyButton.setOnClickListener { showHistoryFragment(fragmentView) }

        optionsMenu = PopupMenu(requireContext(), optionsButton)
        optionsMenu.menuInflater.inflate(R.menu.options_menu, optionsMenu.menu)
        optionsMenu.setOnMenuItemClickListener {
            var result = true;

            when (it.itemId) {
                R.id.settingsButton -> {
                    showSettingsFragment(fragmentView)
                }
                R.id.aboutButton -> {
                    showAboutFragment(fragmentView)
                }
                else -> {
                    result = false;
                }
            }

            return@setOnMenuItemClickListener result
        }
    }

    private fun callOnInTextChange(text: String) {
        CalculatorInputStorage.mathTextData = MathTextData(text)
        cancelSaveToHistoryTask()

        if (inTextView.text.isEmpty()) {
            solutionView.hideCurrentView()
            calculatorProcessor.stopCurrentCalculations()
        } else if (!inTextView.isComplete) {
            solutionView.showIncompleteInput()
            calculatorProcessor.stopCurrentCalculations()
        } else {
            calculatorProcessor.calculate(text)
        }
    }

    private fun outTexts(texts: List<String>) {
        if (texts.first() == getString(R.string.invalid_input)) {
            solutionView.showInvalidInput()
        } else if (texts.first() == getString(R.string.character_limit_exceeded)) {
            solutionView.showCharacterLimitExceeded()
        } else {
            solutionView.showSolution(texts)

            saveToHistoryTask = Timer().schedule(saveToHistoryDelay) {
                requireActivity().runOnUiThread {
                    callOnSaveToHistory()
                }
            }
        }
    }

    private fun startLoading() {
        solutionView.showLoading()
    }

    private fun callOnInTextFocusChange(state: Boolean) {
        if (!state) {
            inTextView.requestFocus()
        }
    }

    private fun touchInText(event: MotionEvent): Boolean {
        return inTextView.dispatchTouchEvent(MotionEvent.obtain(
            event.downTime, event.eventTime, event.action, event.x, 0f, event.metaState
        ))
    }

    private fun callOnSaveToHistory() {
        HistoryStorage.saveItem(inTextView.text)
        cancelSaveToHistoryTask()
    }

    private fun showOptionsMenu(view: View) {
        optionsMenu.show()
    }

    private fun showHistoryFragment(view: View) {
        runSaveToHistoryTask()
        inTextView.clearFocus()
        view.findNavController().navigate(R.id.action_calculatorFragment_to_historyFragment)
    }

    private fun showCameraFragment(view: View) {
        // TODO: implement
    }

    private fun showAboutFragment(view: View) {
        runSaveToHistoryTask()
        inTextView.clearFocus()
        view.findNavController().navigate(R.id.action_calculatorFragment_to_aboutFragment)
    }

    private fun showSettingsFragment(view: View) {
        runSaveToHistoryTask()
        inTextView.clearFocus()
        view.findNavController().navigate(R.id.action_calculatorFragment_to_settingsFragment)
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
