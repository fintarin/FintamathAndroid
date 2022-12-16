package com.fintamath.mathtextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.fintamath.KeyboardKeyCode;
import com.fintamath.R;

import java.util.ArrayList;
import java.util.List;

public class MathTextView extends LinearLayout {

    private List<View> mViews = new ArrayList<>();
    private EditText mCurrentTextView;

    private int mTextViewLayout;
    private String mHintText;
    private final String mHintInnerText;
    private OnTouchListener mOnTouchListener;

    private int mCursorPosition;
    private StringBuilder mStringBuilder = new StringBuilder();

    public MathTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MathTextView);
        int n = a.getIndexCount();

        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);

            switch (attr){
                case R.styleable.MathTextView_textViewLayout: {
                    mTextViewLayout = a.getResourceId(attr, 0);
                    break;
                }
                case R.styleable.MathTextView_hint: {
                    mHintText = a.getString(attr);
                    break;
                }
                default: {
                    break;
                }
            }
        }

        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mViews.add(inflate.inflate(mTextViewLayout, null));
        mCurrentTextView = (EditText) mViews.get(0);
        mHintInnerText = mCurrentTextView.getHint().toString();
        addView(mCurrentTextView);

        setText("");
    }

    public void insert(String text) {
        if (" ".equals(text)) {
            return;
        }

        mStringBuilder.insert(mCursorPosition, text);
        setText(mStringBuilder.toString());
        moveCursor(text.length());
    }

    public void insertBrackets() {
        insert("()");
        moveCursor(-1);
    }

    public void insertUnaryFunction(KeyboardKeyCode func) {
        String text = func.toString().toLowerCase();
        insert(text);
        insertBrackets();
    }

    public void insertBinaryFunction(KeyboardKeyCode func) {
        String text = func.toString().toLowerCase();
        insert(text + "(,)");
        moveCursor(-2);
    }

    public void delete() {
        if (mCursorPosition == 0) {
            return;
        }

        int cursorPosition = mCursorPosition - 1;
        int length = 1;

        if (cursorPosition >= 0 && cursorPosition + 1 < getText().length() &&
                getText().charAt(cursorPosition) == '(' &&
                getText().charAt(cursorPosition + 1) == ')') {
            length++;
        }

        mStringBuilder.delete(cursorPosition, cursorPosition + length);
        setText(mStringBuilder.toString());
        setCursorPositions(cursorPosition);
    }

    public void clear() {
        setText("");
        setCursorPositions(0);
    }

    public void moveCursor(int i) {
        setCursorPositions(mCursorPosition + i);
    }

    public int getCursorPosition() {
        return mCursorPosition;
    }

    @Override
    public void setOnTouchListener(OnTouchListener onTouchListener) {
        mOnTouchListener = onTouchListener;
        mCurrentTextView.setOnTouchListener(onTouchListener);
    }

    private void setText(String inText) {
        mStringBuilder = new StringBuilder(inText);
        mCurrentTextView.setText(mStringBuilder.toString());

        if ("".equals(mStringBuilder.toString())) {
            mCurrentTextView.setHint(mHintText);
        } else {
            mCurrentTextView.setHint(mHintInnerText);
        }
    }

    private void setCursorPositions(int pos) {
        if (pos >= 0 && pos <= mCurrentTextView.getText().length()) {
            mCursorPosition = pos;
            mCurrentTextView.setSelection(mCursorPosition);
        }
    }

    public String getText() {
        return mStringBuilder.toString();
    }
}
