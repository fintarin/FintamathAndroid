package com.fintamath.calculator

import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.schedule
import kotlin.concurrent.thread

class CalculatorProcessor(
    private val callbacksThread: (() -> Unit) -> Unit,
    private val outTextsCallback: (List<String>) -> Unit,
    private val loadingCallback: () -> Unit,
) {

    private val loadingDelay: Long = 100
    private var loadingTask: TimerTask? = null

    private val calculator: Calculator = Calculator { onCalculated(it) }
    private var isCalculating = AtomicBoolean(false)

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

    private fun onCalculated(result: List<String>) {
        callbacksThread {
            isCalculating.set(false)
            outTextsCallback.invoke(result)
        }
    }
}
