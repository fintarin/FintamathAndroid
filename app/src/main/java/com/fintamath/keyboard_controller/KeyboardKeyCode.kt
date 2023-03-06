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
    Undo(6033),
    Redo(6034),
    ;

    companion object {
        private val VALUES = values()

        fun fromInt(value: Int) = VALUES.firstOrNull { it.value == value }
    }
}
