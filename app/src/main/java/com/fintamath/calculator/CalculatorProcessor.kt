package com.fintamath.calculator

import android.app.Activity
import com.fintamath.textview.MathEditText
import com.fintamath.textview.MathAlternativesTextView

class CalculatorProcessor(
    private val activity: Activity,
    private val inTextView: MathEditText,
    private val outTextView: MathAlternativesTextView
) {

    private val calculator: Calculator = Calculator()
    private var calcThread: Thread? = null

    fun calculate() {
        if (calcThread != null && calcThread!!.isAlive) {
            calcThread!!.interrupt()
        }

        var text = inTextView.text
        if (text.isEmpty()) {
            outTextView.setTexts(null)
            return
        }

        calcThread = Thread {
            activity.runOnUiThread { outTextView.setTexts(listOf(". . .")) }
            val resData = calculator.calculate(text)
            if (Thread.currentThread() === calcThread) {
                activity.runOnUiThread {
                    val results = listOf(*resData.split("\n").toTypedArray())
                    outTextView.setTexts(results)
                }
            }
        }

        calcThread!!.start()
    }
}
