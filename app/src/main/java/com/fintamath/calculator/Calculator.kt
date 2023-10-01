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
