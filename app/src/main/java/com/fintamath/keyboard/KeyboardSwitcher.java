package com.fintamath.keyboard;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.view.View;

import java.util.Map;

import kotlin.Pair;

public class KeyboardSwitcher {

    private final Map<KeyboardType, Pair<KeyboardView, Keyboard>> keyboards;
    private KeyboardView currentKeyboard;
    private KeyboardType currentKeyboardType;

    public KeyboardSwitcher(Map<KeyboardType, Pair<KeyboardView, Keyboard>> keyboards, KeyboardView currentKeyboard, KeyboardType currentKeyboardType) {
        this.keyboards = keyboards;
        this.currentKeyboard = currentKeyboard;
        this.currentKeyboardType = currentKeyboardType;
    }

    public void switchKeyboard(KeyboardType keyboardType) {
        currentKeyboard.setVisibility(View.GONE);
        currentKeyboard = keyboards.get(keyboardType).getFirst();
        currentKeyboard.setVisibility(View.VISIBLE);
        currentKeyboardType = keyboardType;
    }

    public KeyboardType getCurrentKeyboardType() {
        return currentKeyboardType;
    }
}
