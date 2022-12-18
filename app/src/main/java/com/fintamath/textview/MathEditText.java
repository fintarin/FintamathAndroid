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
        insertEditText(mCurrentEditText, -1);
        mCurrentEditText.setHint(mHintText);
        update();
    }

    @Override
    public String getText() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < getChildCount(); i++) {
            String text = getTextFromView(getChildAt(i));
            if (!text.isEmpty() &&
                    text.charAt(text.length() - 1) >= '0' && text.charAt(text.length() - 1) <= '9' &&
                    getChildAt(i + 1) instanceof MathTextViewFraction) {
                stringBuilder.append("(").append(text).append("+").append(getTextFromView(getChildAt(i + 1))).append(")");
                i++;
            } else {
                stringBuilder.append(text);
            }
        }

        if (stringBuilder.toString().isEmpty()) {
            return "";
        }

        for (String key : getTextReplacements.keySet()) {
            stringBuilder = new StringBuilder(stringBuilder.toString().replaceAll(key, getTextReplacements.get(key)));
        }

        return stringBuilder.toString();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof EditText) {
                EditText editText = (EditText) view;
                if (!editText.getText().toString().isEmpty()) {
                    return false;
                }
            } else if (view instanceof MathTextViewBase) {
                MathTextViewBase mathTextView = (MathTextViewBase) view;
                if (!mathTextView.isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    public void insert(String text) {
        if (" ".equals(text)) {
            return;
        }

        InputConnection inputConnection = mCurrentEditText.onCreateInputConnection(new EditorInfo());
        inputConnection.commitText(text, 1);
        update();
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
        update();
        moveCursorRight();
    }

    public void delete() {
        if (mCurrentEditText.getSelectionStart() == 0) {
            MathTextViewBase currentEditTextParent = (MathTextViewBase) mCurrentEditText.getParent();

            if (!(currentEditTextParent instanceof MathEditText) && currentEditTextParent.isEmpty()) {
                MathEditText newEditTextParent = (MathEditText) currentEditTextParent.getParent();
                int i = newEditTextParent.indexOfChild(currentEditTextParent);

                setCurrentEditText((EditText) newEditTextParent.getChildAt(i - 1));
                mCurrentEditText.setText(mCurrentEditText.getText().toString() +
                        ((EditText) newEditTextParent.getChildAt(i + 1)).getText().toString());
                mCurrentEditText.setSelection(mCurrentEditText.getText().length());

                newEditTextParent.removeViewAt(i);
                newEditTextParent.removeViewAt(i);

                if (newEditTextParent != this && newEditTextParent.getChildCount() == 1) {
                    View newEditTextParentChild = newEditTextParent.getChildAt(0);
                            MathTextViewBase newEditTextParentParent = (MathTextViewBase) newEditTextParent.getParent();
                    int j = newEditTextParentParent.indexOfChild(newEditTextParent);

                    newEditTextParent.removeAllViews();
                    newEditTextParentParent.removeViewAt(j);
                    newEditTextParentParent.addView(newEditTextParentChild, j);
                }
            } else {
                moveCursorLeft();
            }
        } else {
            InputConnection inputConnection = mCurrentEditText.onCreateInputConnection(new EditorInfo());
            int cursorPosition = mCurrentEditText.getSelectionStart();

            if (cursorPosition - 1 >= 0 && cursorPosition < mCurrentEditText.length() &&
                    mCurrentEditText.getText().charAt(cursorPosition - 1) == '(' &&
                    mCurrentEditText.getText().charAt(cursorPosition) == ')') {
                inputConnection.deleteSurroundingText(1, 1);
                return;
            }

            inputConnection.deleteSurroundingText(1, 0);
        }

        if (mCurrentEditText.getText().toString().isEmpty()) {
            if (getChildCount() == 1) {
                mCurrentEditText.setHint(mHintText);
            } else {
                mCurrentEditText.setHint(mInnerHintText);
            }
        }

        update();
    }

    public void clear() {
        removeAllViews();
        insertEditText((EditText) inflate.inflate(mEditTextLayout, null), -1);
        mCurrentEditText = (EditText) getChildAt(0);
        mCurrentEditText.requestFocus();
        mCurrentEditText.setHint(mHintText);
        update();
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
    protected void update() {
        updateViewsRec(this);
    }

    private void updateViewsRec(MathTextViewBase mathTextView) {
        for (int childNum = 0; childNum < mathTextView.getChildCount(); childNum++) {
            final View child = mathTextView.getChildAt(childNum);
            if (child instanceof MathTextViewBase) {
                updateViewsRec((MathTextViewBase) child);
                ((MathTextViewBase) child).update();
            }
        }
    }

    @Override
    public void setOnTouchListener(OnTouchListener listener) {
        super.setOnTouchListener(listener);
        mOnTouchListener = listener;
    }

    private void insertEditText(EditText editText, int i) {
        setCommonLayoutParams(editText);
        editText.setHint("");
        setEditTextOnTouchEventListener(editText);
        addView(editText, i);
    }

    private void insertMathTextView(MathTextViewBase insertedMathTextView) {
        setCommonLayoutParams(insertedMathTextView);
        setEditTextsOnTouchEventListener(insertedMathTextView);

        int selectionStart = mCurrentEditText.getSelectionStart();
        EditText rightEditText = (EditText) inflate.inflate(mEditTextLayout, null);
        rightEditText.setText(mCurrentEditText.getText().toString().substring(selectionStart));

        mCurrentEditText.setText(mCurrentEditText.getText().toString().substring(0, selectionStart));
        mCurrentEditText.setSelection(selectionStart);

        MathTextViewBase currentEditTextParent = (MathTextViewBase) mCurrentEditText.getParent();

        int i = currentEditTextParent.indexOfChild(mCurrentEditText);
        if (currentEditTextParent instanceof MathEditText) {
            mCurrentEditText.setHint("");
            MathEditText newMathEditText = (MathEditText) currentEditTextParent;
            newMathEditText.addView(insertedMathTextView, i + 1);

            if (currentEditTextParent.getChildCount() == i + 2 ||
                    !(currentEditTextParent.getChildAt(i + 2) instanceof EditText)) {
                newMathEditText.insertEditText(rightEditText, i + 2);
            }
        } else {
            MathEditText newMathEditText = new MathEditText(getContext(), mAttrs);
            newMathEditText.setOnTouchListener(mOnTouchListener);
            newMathEditText.mHintText = "";
            newMathEditText.mCurrentEditText.setHint("");

            currentEditTextParent.removeViewAt(i);
            currentEditTextParent.addView(newMathEditText, i);

            newMathEditText.addView(insertedMathTextView);
            newMathEditText.insertEditText(rightEditText, -1);
            newMathEditText.mCurrentEditText.setText(mCurrentEditText.getText());

            setCurrentEditText(newMathEditText.mCurrentEditText);
            mCurrentEditText.setSelection(selectionStart);
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
        if (mCurrentEditText != null) {
            if (mCurrentEditText.getParent() instanceof MathEditText) {
                mCurrentEditText.setHint("");
            }

            mCurrentEditText.clearFocus();
        }

        mCurrentEditText = editText;
        mCurrentEditText.requestFocus();
        mCurrentEditText.setHint(mInnerHintText);
        update();
    }
}
