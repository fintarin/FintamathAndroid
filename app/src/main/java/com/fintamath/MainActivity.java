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
import android.widget.TextView;

import com.fintamath.mathtextview.MathEditText;

import java.util.Map;

import kotlin.Pair;

/**
 * todo
 * сделать, чтобы подсвечивались кнопки abc и f(x) при выборе
 * сделать фон чуть светлее
 * - сделать 2 меню вывода
 * исправить отображение меню ввода или сделать на контейнерах
 * сделать, выделение кнопок, которые вызывают попап
 */
public class MainActivity extends AppCompatActivity {

    private MathEditText inText;
    private TextView outText;

    private Map<KeyboardType, Pair<KeyboardView, Keyboard>> keyboards;
    private KeyboardView currentKeyboard;
    private KeyboardSwitcher keyboardSwitcher;

    private CalculatorProcessor calculatorProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inText = findViewById(R.id.inText);
        inText.setOnTouchListener(this::onTouchInText);

        outText = findViewById(R.id.outText);
        outText.setOnTouchListener(this::onTouchOutText);

        calculatorProcessor = new CalculatorProcessor(this, inText, outText);

        initKeyboards();
        currentKeyboard.setVisibility(View.VISIBLE);

        inText.requestFocus();
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
                        new KeyboardActionListener(calculatorProcessor, keyboardSwitcher, inText, outText)
                ),
                entry(KeyboardType.LettersKeyboard,
                        new KeyboardActionListener(calculatorProcessor, keyboardSwitcher, inText, outText)
                ),
                entry(KeyboardType.FunctionsKeyboard,
                        new KeyboardActionListener(calculatorProcessor, keyboardSwitcher, inText, outText)
                )
        );

        keyboards.forEach((key, value) -> {
            value.getFirst().setKeyboard(value.getSecond());
            value.getFirst().setOnKeyboardActionListener(listeners.get(key));
        });
    }

    private boolean onTouchInText(View view, MotionEvent event) {
        hideSystemKeyboard(view);

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

    private void hideSystemKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
