package com.fintamath.calculator

import androidx.annotation.Keep

@Keep
internal class Calculator(
    private val calculatedCallback: (result: List<String>) -> Unit,
    private val interruptedCallback: () -> Unit,
) {

    external fun calculate(str: String)

    external fun stopCurrentCalculations()

    external fun getPrecision(): Int

    external fun setPrecision(int: Int)

    external fun approximate(exprStr: String, varStr: String, valStr: String): String

    external fun getVariableCount(exprStr: String): Int

    external fun getLastVariable(exprStr: String): String

    private fun onCalculated(str: String) {
        calculatedCallback.invoke(listOf(*str.split("\n").toTypedArray()))
    }

    private fun onInterrupted() {
        interruptedCallback.invoke()
    }

    companion object {
        init {
            System.loadLibrary("fintamath_android")
        }
    }
}
