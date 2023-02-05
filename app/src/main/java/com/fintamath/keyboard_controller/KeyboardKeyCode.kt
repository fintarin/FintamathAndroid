package com.fintamath.keyboard_controller

internal enum class KeyboardKeyCode(val value: Int) {
    Delete(5996),
    Clear(5997),
    MoveLeft(5998),
    MoveRight(5999),
    MainKeyboard(6035),
    LettersKeyboard(6000),
    FunctionsKeyboard(6001),
    AnalysisKeyboard(6037),
    LogicKeyboard(6038),
    Brackets(6002),
    Pow2(6003),
    Pow3(6004),
    PowN(6005),
    Sqrt(6006),
    Sin(6009),
    Cos(6010),
    Tan(6011),
    Cot(6012),
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
    Derivative(6025),
    Frac(6031),
    Root(6032),
    History(6033),
    NewLine(6034),
    ;

    companion object {
        private val VALUES = values()

        fun fromInt(value: Int) = VALUES.firstOrNull { it.value == value }
    }
}
