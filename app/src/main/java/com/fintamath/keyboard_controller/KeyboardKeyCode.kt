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
    Sinh(6039),
    Cosh(6040),
    Tanh(6041),
    Coth(6042),
    Asinh(6043),
    Acosh(6044),
    Atanh(6045),
    Acoth(6046),
    Log(6019),
    Ln(6020),
    Lb(6021),
    Lg(6022),
    Abs(6023),
    Exp(6024),
    Sign(6048),
    Degrees(6052),
    Frac(6031),
    History(6033),
    NewLine(6034),
    F(6036),
    Index1(6047),
    Index2(6050),
    IndexN(6051),
    Derivative(6025),
    ;

    companion object {
        private val VALUES = values()

        fun fromInt(value: Int) = VALUES.firstOrNull { it.value == value }
    }
}
