package com.fintamath.mathtextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.fintamath.KeyboardKeyCode;
import com.fintamath.R;

import java.util.ArrayList;
import java.util.List;

public class MathEditText extends LinearLayout {

    private AttributeSet mAttrs;
    private int mEditTextLayout;
    private int mFractionLineLayout;
    private String mHintText;
    private final String mHintInnerText;
    private OnTouchListener mOnTouchListener;

    private List<View> mViews = new ArrayList<>();
    private EditText mCurrentEditText;

    private int mCursorPosition;
    private StringBuilder mStringBuilder = new StringBuilder();

    public MathEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        mAttrs = attrs;
        TypedArray a = context.obtainStyledAttributes(mAttrs, R.styleable.MathTextView);
        int n = a.getIndexCount();

        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);

            switch (attr) {
                case R.styleable.MathTextView_textViewLayout: {
                    mEditTextLayout = a.getResourceId(attr, 0);
                    break;
                }
                case R.styleable.MathTextView_hint: {
                    mHintText = a.getString(attr);
                    break;
                }
                case R.styleable.MathTextView_fractionLineLayout: {
                    mFractionLineLayout = a.getResourceId(attr, 0);
                    break;
                }
                default: {
                    break;
                }
            }
        }

        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mViews.add(inflate.inflate(mEditTextLayout, null));
        mCurrentEditText = (EditText) mViews.get(0);
        mHintInnerText = mCurrentEditText.getHint().toString();
        addView(mCurrentEditText);

//        LinearLayout vbox = new LinearLayout(getContext());
//        vbox.setOrientation(VERTICAL);
//
//        vbox.addView(inflate.inflate(mEditTextLayout, null));
//        vbox.addView(inflate.inflate(mFractionLineLayout, null));
//        vbox.addView(inflate.inflate(mEditTextLayout, null));
//
//        addView(vbox);

        setText("");
    }

    public String getText() {
        return mStringBuilder.toString();
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

    // TODO
//    @Override
//    public void setOnTouchListener(OnTouchListener onTouchListener) {
//        mOnTouchListener = onTouchListener;
//        mCurrentEditText.setOnTouchListener(onTouchListener);
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        return mCurrentEditText.dispatchTouchEvent(event);
//    }

    private void setText(String inText) {
        mStringBuilder = new StringBuilder(inText);
        mCurrentEditText.setText(mStringBuilder.toString());

        if ("".equals(mStringBuilder.toString())) {
            mCurrentEditText.setHint(mHintText);
        } else {
            mCurrentEditText.setHint(mHintInnerText);
        }
    }

    private void setCursorPositions(int pos) {
        if (pos >= 0 && pos <= mCurrentEditText.getText().length()) {
            mCursorPosition = pos;
            mCurrentEditText.setSelection(mCursorPosition);
        }
    }
}
