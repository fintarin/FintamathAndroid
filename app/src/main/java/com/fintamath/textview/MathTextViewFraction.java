package com.fintamath.textview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.fintamath.R;

class MathTextViewFraction extends MathTextViewBase {

    public static final int NUMERATOR_ID = 0;
    public static final int LINE_ID = 1;
    public static final int DENOMINATOR_ID = 2;

    private static final int FRACTION_LINE_HEIGHT = 1;

    private int mEditTextLayout;
    private int mFractionLineLayout;

    public MathTextViewFraction(Context context, TypedArray attrArray) {
        super(context);

        setOrientation(VERTICAL);

        int n = attrArray.getIndexCount();

        for (int i = 0; i < n; i++) {
            int attr = attrArray.getIndex(i);

            switch (attr) {
                case R.styleable.MathTextView_textViewLayout: {
                    mEditTextLayout = attrArray.getResourceId(attr, 0);
                    break;
                }
                case R.styleable.MathTextView_fractionLineLayout: {
                    mFractionLineLayout = attrArray.getResourceId(attr, 0);
                    break;
                }
                default: {
                    break;
                }
            }
        }

        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        addView(inflate.inflate(mEditTextLayout, null));
        addView(inflate.inflate(mFractionLineLayout, null));
        addView(inflate.inflate(mEditTextLayout, null));

        setCommonLayoutParams(getChildAt(NUMERATOR_ID));
        setCommonLayoutParams(getChildAt(DENOMINATOR_ID));
    }

    @Override
    public String getText() {
        return "(" + getTextFromView(getChildAt(NUMERATOR_ID)) + ")/(" + getTextFromView(getChildAt(DENOMINATOR_ID)) + ")";
    }

    @Override
    protected void update() {
        getChildAt(NUMERATOR_ID).measure(0, 0);
        getChildAt(DENOMINATOR_ID).measure(0, 0);

        int width = Math.max(getChildAt(NUMERATOR_ID).getMeasuredWidth(), getChildAt(DENOMINATOR_ID).getMeasuredWidth());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, FRACTION_LINE_HEIGHT, getResources().getDisplayMetrics());
        getChildAt(LINE_ID).setLayoutParams(new LayoutParams(width, height));
    }
}
