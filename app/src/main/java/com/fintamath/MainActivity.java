package com.fintamath;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

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
    private InputMethodService inputMethodService;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calculator = new Calculator();

        inText = findViewById(R.id.inText);
        outText = findViewById(R.id.outText);

        keyboardView = findViewById(R.id.keyboard_view);
        keyboardView.setKeyboard(new Keyboard(this, R.xml.keyboard));

        inText.setOnTouchListener(this::onTouchInText);
        outText.setOnTouchListener(this::onTouchOutText);
        keyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);

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

    private KeyboardView.OnKeyboardActionListener mOnKeyboardActionListener = new KeyboardView.OnKeyboardActionListener() {
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
    };
}
