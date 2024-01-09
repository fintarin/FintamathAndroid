package com.fintamath.calculator

class Approximator {

    external fun approximate(exprStr: String, varStr: String, valStr: String): String

    external fun getVariableCount(exprStr: String): Int

    external fun getLastVariable(exprStr: String): String

    companion object {
        init {
            System.loadLibrary("fintamath_android")
        }
    }
}