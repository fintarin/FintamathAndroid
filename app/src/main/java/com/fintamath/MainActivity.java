package com.fintamath;

import static java.util.Map.entry;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Map;

import kotlin.Pair;

/**
 * todo
 * 7) исправить символ дроби
 * сделать фокус на popup keyboard
 * сделать окантовку клавиш
 * сделать все keyLabel lowercase
 * сделать, чтобы подсвечивались кнопки abc и f(x) при выборе
 */
public class MainActivity extends AppCompatActivity {

    private Calculator calculator;

    private EditText inText;
    private TextView outText;

    private Map<KeyboardType, Pair<KeyboardView, Keyboard>> keyboards;
    private KeyboardView currentKeyboard;
    private KeyboardSwitcher keyboardSwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calculator = new Calculator();

        inText = findViewById(R.id.inText);
        inText.setOnTouchListener(this::onTouchInText);

        outText = findViewById(R.id.outText);
        outText.setOnTouchListener(this::onTouchOutText);

        initKeyboards();

        inText.requestFocus();
        currentKeyboard.setVisibility(View.VISIBLE);
    }

    private void initKeyboards() {
        keyboards = Map.ofEntries(
                entry(KeyboardType.MainKeyboard, new Pair<>(
                        findViewById(R.id.main_keyboard_view),
                        new Keyboard(this, R.xml.keyboard_main)
                )),
                entry(KeyboardType.LettersKeyboard, new Pair<>(
                        findViewById(R.id.letters_keyboard_view),
                        new Keyboard(this, R.xml.keyboard_letters)
                )),
                entry(KeyboardType.FunctionsKeyboard, new Pair<>(
                        findViewById(R.id.functions_keyboard_view),
                        new Keyboard(this, R.xml.keyboard_functions)
                ))
        );

        KeyboardType currentKeyboardType = KeyboardType.MainKeyboard;
        currentKeyboard = keyboards.get(currentKeyboardType).getFirst();
        keyboardSwitcher = new KeyboardSwitcher(keyboards, currentKeyboard, currentKeyboardType);

        Map<KeyboardType, KeyboardView.OnKeyboardActionListener> listeners = Map.ofEntries(
                entry(KeyboardType.MainKeyboard,
                        new KeyboardActionListener(calculator, keyboardSwitcher, inText, outText)
                ),
                entry(KeyboardType.LettersKeyboard,
                        new KeyboardActionListener(calculator, keyboardSwitcher, inText, outText)
                ),
                entry(KeyboardType.FunctionsKeyboard,
                        new KeyboardActionListener(calculator, keyboardSwitcher, inText, outText)
                )
        );

        keyboards.forEach((key, value) -> {
            value.getFirst().setKeyboard(value.getSecond());
            value.getFirst().setOnKeyboardActionListener(listeners.get(key));
        });
    }

    private boolean onTouchInText(View view, MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        if (view.equals(inText)) {
            inText.requestFocus();
            currentKeyboard.setVisibility(View.VISIBLE);
        }

        return true;
    }

    private boolean onTouchOutText(View view, MotionEvent motionEvent) {
        if (view.equals(outText)) {
            currentKeyboard.setVisibility(View.GONE);
            inText.clearFocus();
        }

        return true;
    }
}
