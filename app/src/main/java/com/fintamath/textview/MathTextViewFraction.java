package com.fintamath.textview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;
import android.view.LayoutInflater;

import com.fintamath.R;

@SuppressLint("ViewConstructor")
class MathTextViewFraction extends MathTextViewBase {

    public static final int NUMERATOR_ID = 0;
    public static final int LINE_ID = 1;
    public static final int DENOMINATOR_ID = 2;

    private static final int FRACTION_LINE_HEIGHT = 1;

    public MathTextViewFraction(Context context, TypedArray attrs) {
        super(context);

        setOrientation(VERTICAL);

        int mEditTextLayout = attrs.getResourceId(R.styleable.MathTextView_nestedTextViewLayout, 0);
        int mFractionLineLayout = attrs.getResourceId(R.styleable.MathTextView_fractionLineLayout, 0);

        attrs.recycle();

        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        addView(inflate.inflate(mEditTextLayout, null));
        addView(inflate.inflate(mFractionLineLayout, null));
        addView(inflate.inflate(mEditTextLayout, null));

        setCommonParams(getChildAt(NUMERATOR_ID));
        setCommonParams(getChildAt(DENOMINATOR_ID));
    }

    @Override
    public String getText() {
        return "(" + getTextFromView(getChildAt(NUMERATOR_ID)) + ")/(" + getTextFromView(getChildAt(DENOMINATOR_ID)) + ")";
    }

    @Override
    public boolean isEmpty() {
        return getTextFromView(getChildAt(NUMERATOR_ID)).isEmpty() && getTextFromView(getChildAt(DENOMINATOR_ID)).isEmpty();
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
