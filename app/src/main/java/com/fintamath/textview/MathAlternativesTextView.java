package com.fintamath.textview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.fintamath.R;

import java.util.List;

public class MathAlternativesTextView extends LinearLayout {

    private int mTextViewLayout;
    private int mDelimiterLayout;
    private int mLayout;

    private LayoutInflater mInflate;
    private TextView mMainTextView;

    public MathAlternativesTextView(Context context) {
        this(context, null);
    }

    public MathAlternativesTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setOrientation(VERTICAL);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MathAlternativesTextView);
        int n = a.getIndexCount();

        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);

            switch (attr) {
                case R.styleable.MathAlternativesTextView_alternativeLayout: {
                    mLayout = a.getResourceId(attr, 0);
                    break;
                }
                case R.styleable.MathAlternativesTextView_alternativeTextViewLayout: {
                    mTextViewLayout = a.getResourceId(attr, 0);
                    break;
                }
                case R.styleable.MathAlternativesTextView_alternativeDelimiterLayout: {
                    mDelimiterLayout = a.getResourceId(attr, 0);
                    break;
                }
                default: {
                    break;
                }
            }
        }

        mInflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mMainTextView = (TextView) mInflate.inflate(mTextViewLayout, null);
        addTextView(mMainTextView);
    }

    public void setTexts(@Nullable List<String> texts) {
        removeViews(1, getChildCount() - 1);

        if (texts == null || texts.isEmpty()) {
            mMainTextView.setText("");
            return;
        }

        mMainTextView.setText(texts.get(0));

        for (int i = 1; i < texts.size(); i++) {
            if (!isTextUnique(texts.get(i))) {
                continue;
            }

            TextView alternativeTextView = (TextView) mInflate.inflate(mTextViewLayout, null);
            addView(mInflate.inflate(mDelimiterLayout, null));
            addTextView(alternativeTextView);
            alternativeTextView.setText(texts.get(i));
        }
    }

    private void addTextView(TextView textView) {
        ViewGroup scrollView = (ViewGroup) mInflate.inflate(mLayout, null);
        scrollView.setForegroundGravity(getForegroundGravity());
        scrollView.addView(textView);
        addView(scrollView);
    }

    private boolean isTextUnique(String text) {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                TextView textView = (TextView) viewGroup.getChildAt(0);
                if (textView.getText().toString().equals(text)) {
                    return false;
                }
            }
        }

        return true;
    }
}
