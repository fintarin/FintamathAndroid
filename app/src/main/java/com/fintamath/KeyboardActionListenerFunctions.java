package com.fintamath;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;
import android.widget.TextView;

public class KeyboardActionListenerFunctions implements KeyboardView.OnKeyboardActionListener {

    private final Calculator calculator;
    private final KeyboardSwitcher keyboardSwitcher;
    private final EditText inText;
    private final TextView outText;

    KeyboardActionListenerFunctions(Calculator calculator, KeyboardSwitcher keyboardSwitcher, EditText inText, TextView outText) {
        this.calculator = calculator;
        this.keyboardSwitcher = keyboardSwitcher;
        this.inText = inText;
        this.outText = outText;
    }

    @Override public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = inText.onCreateInputConnection(new EditorInfo());
        if (ic == null) {
            return;
        }

        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1, 0);
                break;
            case Keyboard.KEYCODE_MODE_CHANGE:
                keyboardSwitcher.switchKeyboard(KeyboardType.LettersKeyboard);
                break;
            case  Keyboard.KEYCODE_ALT:
                keyboardSwitcher.switchKeyboard(KeyboardType.MainKeyboard);
                break;
            default:
                char code = (char) primaryCode;
                ic.commitText(String.valueOf(code), 1);
        }

        outText.setText(calculator.simplify(inText.getText().toString()));
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