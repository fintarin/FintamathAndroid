package com.fintamath.fragment.graph

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.fintamath.R
import com.fintamath.calculator.Approximator
import com.fintamath.databinding.FragmentGraphBinding
import com.fintamath.storage.CalculatorStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.math.BigDecimal

class GraphFragment : Fragment() {

    private lateinit var viewBinding: FragmentGraphBinding
    private lateinit var approximator: Approximator

    private var currentMathText = CalculatorStorage.outputMathTextData.text

    private val drawGraphScope = CoroutineScope(Job() + Dispatchers.Main)
    private var drawGraphJob: Job? = null

    private val initialMinX = BigDecimal(-10)
    private val initialMaxX = BigDecimal(10)
    private val graphPointNum = 200

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewBinding = FragmentGraphBinding.inflate(inflater, container, false)

        initBarButtons()
        initApproximator()

        viewBinding.graphView.setOnScrollOrScale {
            if (currentMathText != CalculatorStorage.outputMathTextData.text) {
                currentMathText = CalculatorStorage.outputMathTextData.text
                viewBinding.graphView.clearGraph()
            }

            drawGraph(viewBinding.graphView.getMinX(), viewBinding.graphView.getMaxX())
        }

        drawGraph(initialMinX, initialMaxX)

        return viewBinding.root
    }

    private fun initBarButtons() {
        viewBinding.calculatorButton.setOnClickListener { showCalculatorFragment() }
        viewBinding.historyButton.setOnClickListener { showHistoryFragment() }
        // viewBinding.cameraButton.setOnClickListener { showCameraFragment() } // TODO: uncomment when camera is implemented{
        viewBinding.settingsButton.setOnClickListener { showSettingsFragment() }
        viewBinding.aboutButton.setOnClickListener { showAboutFragment() }
    }

    private fun initApproximator() {
        approximator = Approximator()
    }

    private fun drawGraph(minX: BigDecimal, maxX: BigDecimal) {
        drawGraph(CalculatorStorage.outputMathTextData.text, minX, maxX)
    }

    private fun drawGraph(firstSolutionText: String, min: BigDecimal, max: BigDecimal) {
        drawGraphJob?.cancel()

        drawGraphJob = drawGraphScope.launch {
            if (approximator.getVariableCount(firstSolutionText) != 1) {
                return@launch
            }

            val varName = approximator.getLastVariable(firstSolutionText)
            val mid = (min + max).divide(BigDecimal(2))

            drawGraphPoint(firstSolutionText, varName, mid)

            val delta = (max - min).divide(BigDecimal(graphPointNum))
            var bottom = mid - delta
            var top = mid + delta

            while (bottom >= min) {
                yield()

                drawGraphPoint(firstSolutionText, varName, bottom)
                drawGraphPoint(firstSolutionText, varName, top)

                bottom -= delta
                top += delta
            }
        }
    }

    private fun drawGraphPoint(
        firstSolutionText: String,
        varName: String,
        varValue: BigDecimal
    ) {
        if (viewBinding.graphView.hasPoint(varValue)) {
            return
        }

        val approxValueStr = approximator.approximate(
            firstSolutionText,
            varName,
            toFintamathReal(varValue)
        )

        if (approxValueStr.isEmpty()) {
            return
        }

        viewBinding.graphView.addPoint(varValue, toBigDecimal(approxValueStr))
    }

    private fun toFintamathReal(bigDecimal: BigDecimal): String {
        return bigDecimal.toString().replace("E", "*10^")
    }

    private fun toBigDecimal(fintamathReal: String): BigDecimal {
        return BigDecimal(fintamathReal.replace("*10^", "E"))
    }

    private fun showCalculatorFragment() {
        executeBack()
    }

    private fun showHistoryFragment() {
        executeBack()
        showFragment(R.id.action_calculatorFragment_to_historyFragment)
    }

    private fun showCameraFragment() {
        executeBack()
        showFragment(R.id.action_calculatorFragment_to_cameraFragment)
    }

    private fun showAboutFragment() {
        showFragment(R.id.action_graphFragment_to_aboutFragment)
    }

    private fun showSettingsFragment() {
        showFragment(R.id.action_graphFragment_to_settingsFragment)
    }

    private fun showFragment(navigationId: Int) {
        viewBinding.root.findNavController().navigate(navigationId)
    }

    private fun executeBack() {
        viewBinding.root.findNavController().navigateUp()
    }
}
