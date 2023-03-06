package com.fintamath.calculator

import java.util.Timer
import kotlin.concurrent.schedule

class CalculatorProcessor(
    private val outTextsCallback: (result: List<String>) -> Unit,
    private val loadingCallback: () -> Unit
) {

    private val loadDelay: Long = 100

    private val calculator: Calculator = Calculator()
    private var calcThread: Thread? = null

    fun calculate(exprStr : String) {
        if (calcThread != null && calcThread!!.isAlive) {
            calcThread!!.interrupt()
            calcThread = null
        }

        if (exprStr.isEmpty()) {
            outTextsCallback.invoke(listOf(exprStr))
            return
        }

        calcThread = Thread {
            val resData = calculator.calculate(exprStr)

            if (Thread.currentThread() === calcThread) {
                outTextsCallback.invoke(listOf(*resData.split("\n").toTypedArray()))
            }
        }

        calcThread!!.start()

        Timer().schedule(loadDelay) {
            if (calcThread != null && calcThread!!.isAlive) {
                loadingCallback.invoke()
            }
        }
    }
}
