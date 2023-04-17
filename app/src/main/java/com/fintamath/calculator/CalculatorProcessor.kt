package com.fintamath.calculator

import java.util.Timer
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.schedule

class CalculatorProcessor(
    private val outTextsCallback: (result: List<String>) -> Unit,
    private val loadingCallback: () -> Unit
) {

    private val loadingDelay: Long = 100

    private val calculationCallback: (result: List<String>) -> Unit = { onCalculatedThreadChecked(it) }

    private val calculator: Calculator = Calculator(calculationCallback)
    private var calcThread: Thread? = null
    private var calcThreadId: AtomicLong = AtomicLong(-1)

    fun calculate(str : String) {
        if (str.isEmpty()) {
            onCalculated(listOf(""))
            return
        }

        calcThread = Thread {
            calculator.calculate(str)
        }

        calcThread!!.start()

        val calcThreadLocalId = calcThread!!.id
        calcThreadId.set(calcThreadLocalId)

        Timer().schedule(loadingDelay) {
            if (calcThreadLocalId == calcThreadId.get()) {
                loadingCallback.invoke()
            }
        }
    }

    private fun onCalculatedThreadChecked(result: List<String>) {
        if (Thread.currentThread().id == calcThreadId.get()) {
            onCalculated(result)
        }
    }

    private fun onCalculated(result: List<String>) {
        calcThreadId.set(-1)
        outTextsCallback.invoke(result)
    }
}
