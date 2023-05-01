package com.fintamath.calculator

import java.util.Timer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.schedule
import kotlin.concurrent.thread

class CalculatorProcessor(
    private val outTextsCallback: (result: List<String>) -> Unit,
    private val loadingCallback: () -> Unit
) {

    private val loadingDelay: Long = 100

    private val calculator: Calculator = Calculator { onCalculated(it) }
    private var isCalculating = AtomicBoolean(false)

    fun calculate(str : String) {
        isCalculating.set(true)

        thread {
            calculator.calculate(str)
        }

        Timer().schedule(loadingDelay) {
            if (isCalculating.get()) {
                loadingCallback.invoke()
            }
        }
    }

    fun stopCurrentCalculations() {
        isCalculating.set(false)

        thread {
            calculator.stopCurrentCalculations()
        }
    }

    private fun onCalculated(result: List<String>) {
        isCalculating.set(false)
        outTextsCallback.invoke(result)
    }
}
