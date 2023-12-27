package com.fintamath.calculator

import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.schedule
import kotlin.concurrent.thread
import kotlin.math.abs

class CalculatorProcessor(
    private val callbacksThread: (() -> Unit) -> Unit,
    private val outTextsCallback: (List<String>) -> Unit,
    private val loadingCallback: () -> Unit,
) {

    private val loadingDelay: Long = 100
    private val loadingDelayOnInterrupted: Long = loadingDelay * 5
    private var loadingTask: TimerTask? = null

    private val calculator: Calculator = Calculator({ onCalculated(it) }, { onInterrupted() })
    private var isCalculating = AtomicBoolean(false)
    private var lastCalculationTime: Long = 0

    fun calculate(str : String) {
        isCalculating.set(true)
        loadingTask?.cancel()

        thread {
            calculator.calculate(str)
        }

        loadingTask = Timer().schedule(loadingDelay) {
            callbacksThread {
                if (isCalculating.get()) {
                    loadingCallback.invoke()
                }
            }
        }
    }

    fun stopCurrentCalculations() {
        isCalculating.set(false)

        thread {
            calculator.stopCurrentCalculations()
        }
    }

    fun getPrecision() = calculator.getPrecision()

    fun setPrecision(precision: Int) = calculator.setPrecision(precision)

    fun approximate(exprStr: String, varStr: String, valStr: String): String =
        calculator.approximate(exprStr, varStr, valStr)

    fun getVariableCount(exprStr: String): Int = calculator.getVariableCount(exprStr)

    fun getLastVariable(exprStr: String): String = calculator.getLastVariable(exprStr)

    private fun onCalculated(result: List<String>) {
        callbacksThread {
            isCalculating.set(false)
            lastCalculationTime = System.currentTimeMillis()
            outTextsCallback.invoke(result)
        }
    }

    private fun onInterrupted() {
        callbacksThread {
            if (isCalculating.get() && abs(lastCalculationTime - System.currentTimeMillis()) > loadingDelayOnInterrupted) {
                loadingCallback.invoke()
            }
        }
    }
}
