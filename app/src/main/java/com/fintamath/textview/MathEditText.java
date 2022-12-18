package com.fintamath.textview;

import static java.util.Map.entry;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

import com.fintamath.R;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MathEditText extends MathTextViewBase {

    private final TypedArray mAttrs;
    private int mEditTextLayout;
    private String mHintText;
    private String mInnerHintText;
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
        init(context);
    }

    public MathEditText(Context context, TypedArray attrs) {
        super(context);
        mAttrs = attrs;
        init(context);
    }

    private void init(Context context) {
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

        mCurrentEditText = (EditText) inflate.inflate(mEditTextLayout, null);
        mInnerHintText = mCurrentEditText.getHint().toString();
        appendEditText(mCurrentEditText);
        invalidate();
    }

    @Override
    public String getText() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < getChildCount(); i++) {
            stringBuilder.append(getTextFromView(getChildAt(i)));
        }

        if (stringBuilder.toString().isEmpty()) {
            return "";
        }

        for (String key : getTextReplacements.keySet()) {
            stringBuilder = new StringBuilder(stringBuilder.toString().replaceAll(key, getTextReplacements.get(key)));
        }

        return stringBuilder.toString();
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
        MathTextViewFraction fractionTextView = new MathTextViewFraction(getContext(), mAttrs);
        insertMathTextView(fractionTextView);
        invalidate();
        moveCursorRight();
    }

    // TODO: implement removing fractions
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

        if (mCurrentEditText.getText().toString().isEmpty() && getChildCount() != 1) {
            mCurrentEditText.setHint(mInnerHintText);
        }
    }

    public void clear() {
        removeAllViews();
        appendEmptyEditText();
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

    private void moveCursorLeftRec(MathTextViewBase mathTextView, AtomicBoolean isCurrentTextFound, AtomicBoolean isNextTextFound) {
        for (int childNum = mathTextView.getChildCount() - 1; childNum >= 0; childNum--) {
            if (isNextTextFound.get()) {
                return;
            }

            final View child = mathTextView.getChildAt(childNum);

            if (child instanceof MathTextViewBase) {
                moveCursorLeftRec((MathTextViewBase) child, isCurrentTextFound, isNextTextFound);
            } else if (child instanceof EditText) {
                if (isCurrentTextFound.get()) {
                    setCurrentEditText((EditText) child);
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

    private void moveCursorRightRec(MathTextViewBase mathTextView, AtomicBoolean isCurrentTextFound, AtomicBoolean isNextTextFound) {
        for (int childNum = 0; childNum < mathTextView.getChildCount(); childNum++) {
            if (isNextTextFound.get()) {
                return;
            }

            final View child = mathTextView.getChildAt(childNum);

            if (child instanceof MathTextViewBase) {
                moveCursorRightRec((MathTextViewBase) child, isCurrentTextFound, isNextTextFound);
            } else if (child instanceof EditText) {
                if (isCurrentTextFound.get()) {
                    setCurrentEditText((EditText) child);
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

    private void invalidateViewsRec(MathTextViewBase mathTextView) {
        for (int childNum = 0; childNum < mathTextView.getChildCount(); childNum++) {
            final View child = mathTextView.getChildAt(childNum);

            if (child instanceof MathTextViewBase) {
                invalidateViewsRec((MathTextViewBase) child);
                child.invalidate();
            }
        }
    }

    private void updateHint() {
        if (mCurrentEditText.getText().toString().isEmpty()) {
            if (getChildCount() == 1) {
                mCurrentEditText.setHint(mHintText);
            } else {
                mCurrentEditText.setHint("");
            }
        }
    }

    @Override
    public void setOnTouchListener(OnTouchListener listener) {
        super.setOnTouchListener(listener);
        mOnTouchListener = listener;
    }

    private void appendEditText(EditText editText) {
        setCommonLayoutParams(editText);
        editText.setHint("");
        setEditTextOnTouchEventListener(editText);
        addView(editText);
    }

    private void appendEmptyEditText() {
        appendEditText((EditText) inflate.inflate(mEditTextLayout, null));
    }

    private void insertMathTextView(MathTextViewBase insertedMathTextView) {
        setCommonLayoutParams(insertedMathTextView);
        setEditTextsOnTouchEventListener(insertedMathTextView);

        EditText rightEditText = (EditText) inflate.inflate(mEditTextLayout, null);
        rightEditText.setText(mCurrentEditText.getText().toString().substring(mCurrentEditText.getSelectionStart()));
        mCurrentEditText.setText(mCurrentEditText.getText().toString().substring(0, mCurrentEditText.getSelectionStart()));

        MathTextViewBase currentParentMathTextView = (MathTextViewBase) mCurrentEditText.getParent();

        for (int i = 0; i < currentParentMathTextView.getChildCount(); i++) {
            final View child = currentParentMathTextView.getChildAt(i);

            if (child == mCurrentEditText) {
                if (currentParentMathTextView instanceof MathEditText) {
                    mCurrentEditText.setHint("");
                    MathEditText newMathTextView = (MathEditText) currentParentMathTextView;
                    newMathTextView.addView(insertedMathTextView);
                    newMathTextView.appendEditText(rightEditText);
                } else {
                    MathEditText newMathTextView = new MathEditText(getContext(), mAttrs);
                    newMathTextView.setOnTouchListener(mOnTouchListener);
                    newMathTextView.mHintText = "";
                    newMathTextView.mCurrentEditText.setHint("");

                    currentParentMathTextView.removeViewAt(i);
                    currentParentMathTextView.addView(newMathTextView, i);

                    newMathTextView.addView(insertedMathTextView);
                    newMathTextView.appendEditText(rightEditText);
                    newMathTextView.mCurrentEditText.setText(mCurrentEditText.getText());

                    setCurrentEditText(newMathTextView.mCurrentEditText);
                }

                break;
            }
        }
    }

    // TODO: implement on text touch
    private void setEditTextOnTouchEventListener(EditText editText) {
        editText.setOnTouchListener((view, me) -> {
            boolean res = mOnTouchListener.onTouch(this, me);
            setCurrentEditText((EditText) view);
            return res;
        });
    }

    private void setEditTextsOnTouchEventListener(MathTextViewBase mathTextView) {
        for (int i = 0; i < mathTextView.getChildCount(); i++) {
            final View child = mathTextView.getChildAt(i);
            if (child instanceof EditText) {
                setEditTextOnTouchEventListener((EditText) child);
            }
        }
    }

    private void setCurrentEditText(EditText editText) {
        if (mCurrentEditText.getHint() == mInnerHintText) {
            mCurrentEditText.setHint("");
        }

        mCurrentEditText.clearFocus();
        mCurrentEditText = editText;
        mCurrentEditText.requestFocus();

        if (mCurrentEditText.getHint().toString().isEmpty()) {
            mCurrentEditText.setHint(mInnerHintText);
        }
    }
}
