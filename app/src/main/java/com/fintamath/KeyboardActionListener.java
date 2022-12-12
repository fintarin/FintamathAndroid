package com.fintamath;

import android.inputmethodservice.KeyboardView;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;
import android.widget.TextView;

public class KeyboardActionListener implements KeyboardView.OnKeyboardActionListener {

    private final CalculatorProcessor calculatorProcessor;
    private final KeyboardSwitcher keyboardSwitcher;

    private final EditText inText;
    private final TextView outText;

    private final InputConnection inputConnection;

    KeyboardActionListener(CalculatorProcessor calculatorProcessor, KeyboardSwitcher keyboardSwitcher, EditText inText, TextView outText) {
        this.calculatorProcessor = calculatorProcessor;
        this.keyboardSwitcher = keyboardSwitcher;

        this.inText = inText;
        this.outText = outText;

        this.inputConnection = inText.onCreateInputConnection(new EditorInfo());
    }

    @Override public void onKey(int primaryCode, int[] keyCodes) {
        KeyboardKeyCode keyCode = KeyboardKeyCode.valueOf(primaryCode);

        if (keyCode == null) {
            insetText(String.valueOf((char) primaryCode));
            calculatorProcessor.calculate();
            return;
        }

        switch (keyCode) {
            case Delete:
                delete();
                break;
            case DeleteAll:
                inText.setText("");
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
                moveCursor(-1);
                return;
            case MoveRight:
                moveCursor(1);
                return;
            case Brackets:
                insertBrackets();
                break;
            case Mul:
                inputConnection.commitText("*", 1);
                break;
            case Div:
                inputConnection.commitText("/", 1);
                break;
            case DoubleFactorial:
                insetText("!!");
                break;
            case Pi:
                insetText("pi");
                break;
            case Log:
                insertBinaryFunction(keyCode);
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
                insertUnaryFunction(keyCode);
                break;
            case Pow2:
                insetText("^2");
                break;
            case Pow3:
                insetText("^3");
                break;
            case PowN:
                insetText("^");
                break;
            case LessEqv:
                insetText("<=");
                break;
            case MoreEqv:
                insetText(">=");
                break;
            default:
        }

        calculatorProcessor.calculate();
    }

    private void delete() {
        int cursorPosition = inText.getSelectionStart();

        if (cursorPosition - 1 >= 0 && cursorPosition < inText.length() &&
                inText.getText().charAt(cursorPosition - 1) == '(' &&
                inText.getText().charAt(cursorPosition) == ')') {
            inputConnection.deleteSurroundingText(1, 1);
            return;
        }

        inputConnection.deleteSurroundingText(1, 0);
    }

    private void insetText(String text) {
        if (" ".equals(text)) {
            return;
        }

        inputConnection.commitText(text, 1);
    }

    private void insertBrackets() {
        inputConnection.commitText("()", 1);
        moveCursor(-1);
    }

    private void insertUnaryFunction(KeyboardKeyCode func) {
        String text = func.toString().toLowerCase();
        inputConnection.commitText(text, 1);
        insertBrackets();
    }

    private void insertBinaryFunction(KeyboardKeyCode func) {
        String text = func.toString().toLowerCase();
        inputConnection.commitText(text + "(,)", 1);
        moveCursor(-2);
    }

    private void moveCursor(int i) {
        if (inText.getSelectionStart() + i > inText.getText().length()) {
            return;
        }
        if (inText.getSelectionStart() + i < 0) {
            return;
        }

        inText.setSelection(inText.getSelectionStart() + i);
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