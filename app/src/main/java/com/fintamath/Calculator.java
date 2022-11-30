package com.fintamath;

public class Calculator {
    native String simplify(String str);

    native String solve(String str);

    static {
        System.loadLibrary("fintamath_android");
    }
}
