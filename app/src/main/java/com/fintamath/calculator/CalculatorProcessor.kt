package com.fintamath.calculator

import java.util.Timer
import kotlin.concurrent.schedule

class CalculatorProcessor(
    private val outTextsCallback: (result: List<String>) -> Unit,
    private val loadingCallback: () -> Unit
) {

    private val loadingDelay: Long = 100

    private val calculationCallback: (result: List<String>) -> Unit = { onCalculated(it) }

    private val calculator: Calculator = Calculator(outTextsCallback)
    private var calcThread: Thread? = null

    fun calculate(exprStr : String) {
        calcThread = null

        if (exprStr.isEmpty()) {
            calculationCallback.invoke(listOf(exprStr))
            return
        }

        calcThread = Thread {
            calculator.calculate(exprStr)
        }

        calcThread!!.start()

        Timer().schedule(loadingDelay) {
            if (calcThread != null && calcThread!!.isAlive) {
                loadingCallback.invoke()
            }
        }
    }

    private  fun onCalculated(result: List<String>) {
        if (calcThread != null) {
            calcThread = null
            outTextsCallback.invoke(result)
        }
    }
}
