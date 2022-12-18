package com.fintamath.textview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

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

    protected void update() { }

    protected static void setCommonLayoutParams(View view) {
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        view.setLayoutParams(params);
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
