package com.fintamath;

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

/**
 * todo
 * 1) временные фрагменты при долгом нажатии
 * 2) страничка с функциями
 * 3) квадраты во фрагментах
 * 4) логика работы всех операций
 * 5) сверху ввод + разделитель = ответ
 * 6) каретка ввода, кнопки -> <-
 * 7) исправить символ дроби
 * 8) обработать все ошибки
 */
public class MainActivity extends AppCompatActivity {

    private Calculator calculator;

    private EditText inText;
    private TextView outText;

    private KeyboardView keyboardView;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calculator = new Calculator();

        inText = findViewById(R.id.inText);
        inText.setOnTouchListener(this::onTouchInText);

        outText = findViewById(R.id.outText);
        outText.setOnTouchListener(this::onTouchOutText);

        keyboardView = findViewById(R.id.keyboard_view);
        keyboardView.setKeyboard(new Keyboard(this, R.xml.keyboard));
        keyboardView.setOnKeyboardActionListener(new MainKeyboardActionListener(calculator, inText, outText));

        inText.requestFocus();
    }

    private boolean onTouchInText(View view, MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        if (view.equals(inText)) {
            inText.requestFocus();
            keyboardView.setVisibility(View.VISIBLE);
        }

        return true;
    }

    private boolean onTouchOutText(View view, MotionEvent motionEvent) {
        if (view.equals(outText)) {
            keyboardView.setVisibility(View.GONE);
            inText.clearFocus();
        }

        return true;
    }
}
