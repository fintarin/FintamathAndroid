package com.fintamath.textview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.fintamath.R;

class MathTextViewFraction extends LinearLayout {

    private int mEditTextLayout;
    private int mFractionLineLayout;

    private static final int FRACTION_LINE_HEIGHT = 1;

    private static final int NUMERATOR_ID = 0;
    private static final int LINE_ID = 1;
    private static final int DENOMINATOR_ID = 2;

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

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);

            if (child instanceof EditText) {
                LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
                child.setLayoutParams(params);
            }
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();

        getChildAt(NUMERATOR_ID).measure(0, 0);
        getChildAt(DENOMINATOR_ID).measure(0, 0);

        int width = Math.max(getChildAt(NUMERATOR_ID).getMeasuredWidth(), getChildAt(DENOMINATOR_ID).getMeasuredWidth());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, FRACTION_LINE_HEIGHT, getResources().getDisplayMetrics());
        getChildAt(LINE_ID).setLayoutParams(new LayoutParams(width, height));
    }
}
