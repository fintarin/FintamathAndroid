package com.fintamath;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;
import android.widget.TextView;

public class MainKeyboardActionListener implements KeyboardView.OnKeyboardActionListener {

    private final Calculator calculator;

    private final EditText inText;
    private final TextView outText;

    MainKeyboardActionListener(Calculator calculator, EditText inText, TextView outText) {
        this.calculator = calculator;
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
                CharSequence selectedText = ic.getSelectedText(0);

                if (TextUtils.isEmpty(selectedText)) {
                    ic.deleteSurroundingText(1, 0);
                } else {
                    ic.commitText("", 1);
                }

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