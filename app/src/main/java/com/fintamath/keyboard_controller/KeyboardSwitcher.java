package com.fintamath.keyboard_controller;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.view.View;

import java.util.Map;
import java.util.Objects;

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
        if (currentKeyboardType == keyboardType) {
            return;
        }

        currentKeyboard.setVisibility(View.GONE);
        currentKeyboard = Objects.requireNonNull(keyboards.get(keyboardType)).getFirst();
        currentKeyboard.setVisibility(View.VISIBLE);
        currentKeyboardType = keyboardType;
    }

    public KeyboardType getCurrentKeyboardType() {
        return currentKeyboardType;
    }
}
