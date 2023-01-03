package com.fintamath.keyboard_controller;

import android.inputmethodservice.KeyboardView;

import com.fintamath.calculator.CalculatorProcessor;
import com.fintamath.textview.MathEditText;

public class KeyboardActionListener implements KeyboardView.OnKeyboardActionListener {

    private final CalculatorProcessor calculatorProcessor;
    private final KeyboardSwitcher keyboardSwitcher;

    private final MathEditText inText;

    public KeyboardActionListener(CalculatorProcessor calculatorProcessor, KeyboardSwitcher keyboardSwitcher, MathEditText inText) {
        this.calculatorProcessor = calculatorProcessor;
        this.keyboardSwitcher = keyboardSwitcher;

        this.inText = inText;
    }

    @Override public void onKey(int primaryCode, int[] keyCodes) {
        KeyboardKeyCode keyCode = KeyboardKeyCode.valueOf(primaryCode);

        if (keyCode == null) {
            inText.insert(String.valueOf((char) primaryCode));
            keyboardSwitcher.switchKeyboard(KeyboardType.MainKeyboard);
            calculatorProcessor.calculate();
            return;
        }

        switch (keyCode) {
            case MainKeyboard:
                keyboardSwitcher.switchKeyboard(KeyboardType.MainKeyboard);
                return;
            case LettersKeyboard:
                keyboardSwitcher.switchKeyboard(KeyboardType.LettersKeyboard);
                return;
            case FunctionsKeyboard:
                keyboardSwitcher.switchKeyboard(KeyboardType.FunctionsKeyboard);
                return;
            case History:
                // TODO
                return;
            case NewLine:
                // TODO
                return;
            case MoveLeft:
                inText.moveCursorLeft();
                return;
            case MoveRight:
                inText.moveCursorRight();
                return;
            case Delete:
                inText.delete();
                calculatorProcessor.calculate();
                return;
            case Clear:
                inText.clear();
                calculatorProcessor.calculate();
                return;
            case Brackets:
                inText.insertBrackets();
                break;
            case Log:
                inText.insertBinaryFunction(keyCode.toString().toLowerCase());
                break;
            case Sin:
            case Cos:
            case Tan:
            case Cot:
            case Asin:
            case Acos:
            case Atan:
            case Acot:
            case Ln:
            case Lb:
            case Lg:
            case Abs:
            case Exp:
            case Sqrt:
                inText.insertUnaryFunction(keyCode.toString().toLowerCase());
                break;
            case Pow2:
                inText.insert("^2");
                break;
            case Pow3:
                inText.insert("^3");
                break;
            case PowN:
                inText.insert("^");
                break;
            case DoubleFactorial:
                inText.insert("!!");
                break;
            case Frac:
                inText.insertFraction();
                break;
            default:
        }

        keyboardSwitcher.switchKeyboard(KeyboardType.MainKeyboard);
        calculatorProcessor.calculate();
    }

    @Override public void onPress(int arg0) {
    }

    @Override public void onRelease(int primaryCode) {
    }

    @Override public void onText(CharSequence text) {
    }

    @Override public void swipeDown() {
    }

    @Override public void swipeLeft() {
    }

    @Override public void swipeRight() {
    }

    @Override public void swipeUp() {
    }
}