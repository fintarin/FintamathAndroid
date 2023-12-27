package com.fintamath.fragment.graph

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.fintamath.R
import com.fintamath.calculator.Approximator
import com.fintamath.databinding.FragmentGraphBinding
import com.fintamath.storage.CalculatorStorage
import com.fintamath.widget.graph.GraphView
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.math.BigDecimal

class GraphFragment : Fragment() {

    private lateinit var viewBinding: FragmentGraphBinding
    private lateinit var graphView: GraphView

    private var currentFun = CalculatorStorage.outputMathTextData.text

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewBinding = FragmentGraphBinding.inflate(inflater, container, false)
        viewBinding.aboutBackButton.setOnClickListener { executeBack() }

        graphView = viewBinding.root.findViewById(R.id.graphView)
        graphView.setOnScrollOrScale {
            if (currentFun != CalculatorStorage.outputMathTextData.text) {
                currentFun = CalculatorStorage.outputMathTextData.text
                graphView.clearGraph()
            }
            drawGraph(graphView.getMinX(), graphView.getMaxX())
            //drawGraph(graphView.getMinX(), graphView.getMaxX())
        }
        drawGraph(BigDecimal(-10), BigDecimal(10))

        return viewBinding.root
    }

    private fun executeBack() {
        viewBinding.root.findNavController().navigateUp()
    }

    private var drawGraphJob: Job? = null
    private val approximator = Approximator()

    private fun drawGraph(minX: BigDecimal, maxX: BigDecimal) {
        drawGraph(CalculatorStorage.outputMathTextData.text, minX, maxX)
    }

    private fun drawGraph(firstSolutionText: String, min: BigDecimal, max: BigDecimal) {
        drawGraphJob?.cancel()

        drawGraphJob = viewLifecycleOwner.lifecycleScope.launch {
            if (approximator.getVariableCount(firstSolutionText) != 1) {
                return@launch
            }

            val varStr = approximator.getLastVariable(firstSolutionText)
            val mid = (min + max) / BigDecimal(2)

            drawGraphPoint(firstSolutionText, varStr, mid)

            val delta = (max - min) / BigDecimal(200)
            var bottom = mid - delta
            var top = mid + delta

            while (bottom >= min) {
                yield()

                drawGraphPoint(firstSolutionText, varStr, bottom)
                drawGraphPoint(firstSolutionText, varStr, top)

                bottom -= delta
                top += delta
            }
        }
    }

    private fun drawGraphPoint(
        firstSolutionText: String,
        varStr: String,
        top: BigDecimal
    ) {
        graphView.addPoint(top,
        BigDecimal(approximator.approximate(
                firstSolutionText,
                varStr,
                top.toString()
            )))

    }


}
