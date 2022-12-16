package com.fintamath;

import android.inputmethodservice.KeyboardView;
import android.widget.TextView;

import com.fintamath.mathtextview.MathTextView;

public class KeyboardActionListener implements KeyboardView.OnKeyboardActionListener {

    private final CalculatorProcessor calculatorProcessor;
    private final KeyboardSwitcher keyboardSwitcher;

    private final MathTextView inText;
    private final TextView outText;

    KeyboardActionListener(CalculatorProcessor calculatorProcessor, KeyboardSwitcher keyboardSwitcher, MathTextView inText, TextView outText) {
        this.calculatorProcessor = calculatorProcessor;
        this.keyboardSwitcher = keyboardSwitcher;

        this.inText = inText;
        this.outText = outText;
    }

    @Override public void onKey(int primaryCode, int[] keyCodes) {
        KeyboardKeyCode keyCode = KeyboardKeyCode.valueOf(primaryCode);

        if (keyCode == null) {
            inText.insert(String.valueOf((char) primaryCode));
            calculatorProcessor.calculate();
            return;
        }

        switch (keyCode) {
            case Delete:
                inText.delete();
                break;
            case DeleteAll:
                inText.clear();
                break;
            case LettersKeyboard:
                if (keyboardSwitcher.getCurrentKeyboardType() != KeyboardType.LettersKeyboard) {
                    keyboardSwitcher.switchKeyboard(KeyboardType.LettersKeyboard);
                } else {
                    keyboardSwitcher.switchKeyboard(KeyboardType.MainKeyboard);
                }
                break;
            case FunctionsKeyboard:
                if (keyboardSwitcher.getCurrentKeyboardType() != KeyboardType.FunctionsKeyboard) {
                    keyboardSwitcher.switchKeyboard(KeyboardType.FunctionsKeyboard);
                } else {
                    keyboardSwitcher.switchKeyboard(KeyboardType.MainKeyboard);
                }
                break;
            case MoveLeft:
                inText.moveCursor(-1);
                return;
            case MoveRight:
                inText.moveCursor(1);
                return;
            case Brackets:
                inText.insertBrackets();
                break;
            case DoubleFactorial:
                inText.insert("!!");
                break;
            case Pi:
                inText.insert("pi");
                break;
            case Log:
                inText.insertBinaryFunction(keyCode);
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
                inText.insertUnaryFunction(keyCode);
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
            case LessEqv:
                inText.insert("<=");
                break;
            case MoreEqv:
                inText.insert(">=");
                break;
            case Frac:
                inText.insert("/");
                break;
            default:
        }

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