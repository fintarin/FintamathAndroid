package com.fintamath;

import static java.util.Map.entry;

import androidx.appcompat.app.AppCompatActivity;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.fintamath.calculator.CalculatorProcessor;
import com.fintamath.keyboard_controller.KeyboardActionListener;
import com.fintamath.keyboard_controller.KeyboardSwitcher;
import com.fintamath.keyboard_controller.KeyboardType;
import com.fintamath.textview.MathAlternativesTextView;
import com.fintamath.textview.MathEditText;

import java.util.Map;
import java.util.Objects;

import kotlin.Pair;

public class MainActivity extends AppCompatActivity {

    private MathEditText inText;
    private MathAlternativesTextView outText;
    private KeyboardView currentKeyboard;
    private CalculatorProcessor calculatorProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inText = findViewById(R.id.inText);
        outText = findViewById(R.id.outText);
        calculatorProcessor = new CalculatorProcessor(this, inText, outText);

        initKeyboards();
        currentKeyboard.setVisibility(View.VISIBLE);

        inText.requestFocus();
    }

    private void initKeyboards() {
        Map<KeyboardType, Pair<KeyboardView, Keyboard>> keyboards = Map.ofEntries(
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
                )),
                entry(KeyboardType.LogicKeyboard, new Pair<>(
                        findViewById(R.id.logic_keyboard_view),
                        new Keyboard(this, R.xml.keyboard_logic)
                ))
        );

        KeyboardType currentKeyboardType = KeyboardType.MainKeyboard;
        currentKeyboard = Objects.requireNonNull(keyboards.get(currentKeyboardType)).getFirst();
        KeyboardSwitcher keyboardSwitcher = new KeyboardSwitcher(keyboards, currentKeyboard, currentKeyboardType);

        Map<KeyboardType, KeyboardView.OnKeyboardActionListener> listeners = Map.ofEntries(
                entry(KeyboardType.MainKeyboard,
                        new KeyboardActionListener(calculatorProcessor, keyboardSwitcher, inText)
                ),
                entry(KeyboardType.LettersKeyboard,
                        new KeyboardActionListener(calculatorProcessor, keyboardSwitcher, inText)
                ),
                entry(KeyboardType.FunctionsKeyboard,
                        new KeyboardActionListener(calculatorProcessor, keyboardSwitcher, inText)
                ),
                entry(KeyboardType.LogicKeyboard,
                        new KeyboardActionListener(calculatorProcessor, keyboardSwitcher, inText)
                )
        );

        keyboards.forEach((key, value) -> {
            value.getFirst().setKeyboard(value.getSecond());
            value.getFirst().setOnKeyboardActionListener(listeners.get(key));
        });
    }
}
