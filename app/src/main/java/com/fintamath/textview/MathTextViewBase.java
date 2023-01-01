package com.fintamath.textview;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

class MathTextViewBase extends LinearLayout {

    public MathTextViewBase(Context context) {
        super(context);
    }

    public MathTextViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public String getText() {
        return "";
    }

    public boolean isEmpty() {
        return true;
    }

    protected void update() { }

    protected static void setCommonParams(View view) {
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        view.setLayoutParams(params);

        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setShowSoftInputOnFocus(false);
            textView.setLongClickable(false);

            textView.setOnTouchListener((v, event) -> {
                boolean res = textView.onTouchEvent(event);

                textView.clearFocus();

                if (textView.hasSelection()) {
                    int doubleClickDelay = 100;
                    textView.onTouchEvent(MotionEvent.obtain(event.getDownTime() + doubleClickDelay, event.getEventTime(),
                            event.getAction(), event.getX(), event.getY(), event.getMetaState()));
                }

                textView.requestFocus();

                return res;
            });
        }
    }

    protected static String getTextFromView(View view) {
        StringBuilder stringBuilder = new StringBuilder();

        if (view instanceof EditText) {
            EditText editText = (EditText) view;
            stringBuilder.append(editText.getText().toString());
        } else if (view instanceof MathTextViewBase) {
            MathTextViewBase mathTextView = (MathTextViewBase) view;
            stringBuilder.append(mathTextView.getText());
        }

        if (stringBuilder.toString().isEmpty()) {
            return "";
        }

        return stringBuilder.toString();
    }
}
