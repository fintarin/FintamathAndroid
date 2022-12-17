package com.fintamath.calculator;

class Calculator {
    native String calculate(String str);

    static {
        System.loadLibrary("fintamath_android");
    }
}
