package com.fintamath.calculator

internal class Calculator {
    external fun calculate(str: String): String

    companion object {
        init {
            System.loadLibrary("fintamath_android")
        }
    }
}
