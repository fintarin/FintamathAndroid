package com.fintamath.textview;

import static java.util.Map.entry;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.fintamath.R;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MathEditText extends LinearLayout {

    private final TypedArray mAttrs;
    private int mEditTextLayout;
    private String mHintText;
    private OnTouchListener mOnTouchListener;

    private LayoutInflater inflate;
    private EditText mCurrentEditText;

    private final Map<String, String> getTextReplacements = Map.ofEntries(
            entry("÷", "/"),
            entry("×", "*"),
            entry("≤", "<="),
            entry("≥", ">="),
            entry("π", "(pi)")
    );

    public MathEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        mAttrs = context.obtainStyledAttributes(attrs, R.styleable.MathTextView);
        int n = mAttrs.getIndexCount();

        for (int i = 0; i < n; i++) {
            int attr = mAttrs.getIndex(i);

            switch (attr) {
                case R.styleable.MathTextView_textViewLayout: {
                    mEditTextLayout = mAttrs.getResourceId(attr, 0);
                    break;
                }
                case R.styleable.MathTextView_hint: {
                    mHintText = mAttrs.getString(attr);
                    break;
                }
                default: {
                    break;
                }
            }
        }

        inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        addEditText();
        mCurrentEditText = (EditText) getChildAt(0);
        invalidate();
    }

    public String getText() {
        String text = getTextRec(this);

        for (String key : getTextReplacements.keySet()) {
            text = text.replace(key, getTextReplacements.get(key));
        }

        return text.substring(1, text.length() - 1);
    }

    private String getTextRec(LinearLayout layout) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            final View child = layout.getChildAt(i);

            if (child instanceof EditText) {
                EditText editText = (EditText) child;
                return "(" + editText.getText().toString() + ")";
            }
        }

        return "";
    }

    public void insert(String text) {
        if (" ".equals(text)) {
            return;
        }

        InputConnection inputConnection = mCurrentEditText.onCreateInputConnection(new EditorInfo());
        inputConnection.commitText(text, 1);
        invalidate();
    }

    public void insertBrackets() {
        insert("()");
        mCurrentEditText.setSelection(mCurrentEditText.getSelectionStart() - 1);
    }

    public void insertUnaryFunction(String func) {
        insert(func);
        insertBrackets();
    }

    public void insertBinaryFunction(String func) {
        insert(func + "(,)");
        mCurrentEditText.setSelection(mCurrentEditText.getSelectionStart() - 2);
    }

    public void insertFraction() {
        MathTextViewFraction fractionLayout = new MathTextViewFraction(getContext(), mAttrs);
        addLayout(fractionLayout);
        addEditText();
        invalidate();
        moveCursorRight();
    }

    public void delete() {
        InputConnection inputConnection = mCurrentEditText.onCreateInputConnection(new EditorInfo());
        int cursorPosition = mCurrentEditText.getSelectionStart();

        if (cursorPosition - 1 >= 0 && cursorPosition < mCurrentEditText.length() &&
                mCurrentEditText.getText().charAt(cursorPosition - 1) == '(' &&
                mCurrentEditText.getText().charAt(cursorPosition) == ')') {
            inputConnection.deleteSurroundingText(1, 1);
            return;
        }

        inputConnection.deleteSurroundingText(1, 0);
        invalidate();
    }

    public void clear() {
        removeAllViews();
        addEditText();
        mCurrentEditText = (EditText) getChildAt(0);
        mCurrentEditText.requestFocus();
        invalidate();
    }

    public void moveCursorLeft() {
        if (mCurrentEditText.getSelectionStart() > 0) {
            mCurrentEditText.setSelection(mCurrentEditText.getSelectionStart() - 1);
            return;
        }

        moveCursorLeftRec(this, new AtomicBoolean(false), new AtomicBoolean(false));
    }

    private void moveCursorLeftRec(LinearLayout layout, AtomicBoolean isCurrentTextFound, AtomicBoolean isNextTextFound) {
        for (int childNum = layout.getChildCount() - 1; childNum >= 0; childNum--) {
            if (isNextTextFound.get()) {
                return;
            }

            final View child = layout.getChildAt(childNum);

            if (child instanceof LinearLayout) {
                moveCursorLeftRec((LinearLayout) child, isCurrentTextFound, isNextTextFound);
            } else if (child instanceof EditText) {
                if (isCurrentTextFound.get()) {
                    mCurrentEditText.clearFocus();
                    mCurrentEditText = (EditText) child;
                    mCurrentEditText.requestFocus();
                    isNextTextFound.set(true);
                } else if (child == mCurrentEditText) {
                    isCurrentTextFound.set(true);
                }
            }
        }
    }

    public void moveCursorRight() {
        if (mCurrentEditText.getSelectionStart() < mCurrentEditText.length()) {
            mCurrentEditText.setSelection(mCurrentEditText.getSelectionStart() + 1);
            return;
        }

        moveCursorRightRec(this, new AtomicBoolean(false), new AtomicBoolean(false));
    }

    private void moveCursorRightRec(LinearLayout layout, AtomicBoolean isCurrentTextFound, AtomicBoolean isNextTextFound) {
        for (int childNum = 0; childNum < layout.getChildCount(); childNum++) {
            if (isNextTextFound.get()) {
                return;
            }

            final View child = layout.getChildAt(childNum);

            if (child instanceof LinearLayout) {
                moveCursorRightRec((LinearLayout) child, isCurrentTextFound, isNextTextFound);
            } else if (child instanceof EditText) {
                if (isCurrentTextFound.get()) {
                    mCurrentEditText.clearFocus();
                    mCurrentEditText = (EditText) child;
                    mCurrentEditText.requestFocus();
                    isNextTextFound.set(true);
                } else if (child == mCurrentEditText) {
                    isCurrentTextFound.set(true);
                }
            }
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        invalidateViewsRec(this);
        updateHint();
    }

    private void invalidateViewsRec(LinearLayout layout) {
        for (int childNum = 0; childNum < layout.getChildCount(); childNum++) {
            final View child = layout.getChildAt(childNum);

            if (child instanceof LinearLayout) {
                invalidateViewsRec((LinearLayout) child);
                child.invalidate();
            }
        }
    }

    private void updateHint() {
        if (mCurrentEditText.getText().toString().isEmpty()) {
            if (getChildCount() == 1) {
                mCurrentEditText.setHint(mHintText);
            } else {
                mCurrentEditText.setHint(" ");
            }
        }
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        super.setOnTouchListener(l);
        mOnTouchListener = l;
    }

    private void addEditText() {
        EditText editText = (EditText) inflate.inflate(mEditTextLayout, null);

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        editText.setLayoutParams(params);

        editText.setHint(" ");
        editText.setOnTouchListener((view, me) -> mOnTouchListener.onTouch(this, me));

        addView(editText);
    }

    private void addLayout(LinearLayout layout) {
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        layout.setLayoutParams(params);

        addView(layout);

        for (int i = 0; i < layout.getChildCount(); i++) {
            final View child = layout.getChildAt(i);
            if (child instanceof EditText) {
                child.setOnTouchListener((view, me) -> mOnTouchListener.onTouch(this, me));
            }
        }
    }
}
