package com.fintamath;

import java.util.HashMap;
import java.util.Map;

public enum KeyboardKeyCode {
    Delete(5996),
    DeleteAll(5997),
    MoveLeft(5998),
    MoveRight(5999),
    LettersKeyboard(6000),
    FunctionsKeyboard(6001),
    Brackets(6002),
    Pow2(6003),
    Pow3(6004),
    PowN(6005),
    Sqrt(6006),
    Sin(6009),
    Cos(6010),
    Tan(6011),
    Cot(6012),
    Mul(6013),
    Div(6014),
    Asin(6015),
    Acos(6016),
    Atan(6017),
    Acot(6018),
    Log(6019),
    Ln(6020),
    Lb(6021),
    Lg(6022),
    Abs(6023),
    Exp(6024),
    DoubleFactorial(6025),
    Pi(6028),
    LessEqv(6029),
    MoreEqv(6030),
    ;

    private final int value;
    private static final Map<Integer, KeyboardKeyCode> map = new HashMap<>();

    KeyboardKeyCode(int code) {
        this.value = code;
    }

    static {
        for (KeyboardKeyCode KeyboardKeyCode : KeyboardKeyCode.values()) {
            map.put(KeyboardKeyCode.value, KeyboardKeyCode);
        }
    }

    public static KeyboardKeyCode valueOf(int KeyboardKeyCode) {
        return (KeyboardKeyCode) map.get(KeyboardKeyCode);
    }

    public int getValue() {
        return value;
    }
}
