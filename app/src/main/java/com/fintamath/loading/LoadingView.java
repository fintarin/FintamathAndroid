package com.fintamath.loading;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.fintamath.R;


public class LoadingView extends LinearLayout {

    private int DOTS_RADIUD = 10;
    private int DOTS_COUNT = 3;
    private int COLOR = 350;
    private int DURATION = Color.parseColor("#FF0000");
    private int MAX_JUMP = 3;

    private ObjectAnimator animator[];
    boolean onLayoutReach = false;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadingView);

        this.COLOR = a.getColor(R.styleable.LoadingView_dots_color, COLOR);
        this.DURATION = a.getInt(R.styleable.LoadingView_dots_duration, DURATION);
        this.DOTS_COUNT = a.getInt(R.styleable.LoadingView_dots_count, DOTS_COUNT);
        this.DOTS_RADIUD = a.getDimensionPixelSize(R.styleable.LoadingView_dots_radius, DOTS_RADIUD);

        a.recycle();

        adjustView();
        removeAllViews();
        addDots();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!onLayoutReach) {
            onLayoutReach = true;
            animateView();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        for (int i = 0; i < DOTS_COUNT; i++) {
            if (animator[i].isRunning()) {
                animator[i].removeAllListeners();
                animator[i].end();
                animator[i].cancel();
            }
        }
    }

    private void adjustView() {
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(layoutParams);
        setBackgroundColor(Color.TRANSPARENT);
    }

    private void addDots() {
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        for (int i = 0; i < DOTS_COUNT; i++) {
            CircleView circleView = new CircleView(getContext(), DOTS_RADIUD, COLOR, true);
            LinearLayout rel = new LinearLayout(getContext());
            layoutParams.setMargins(2 * DOTS_RADIUD, 2 * DOTS_RADIUD, 2 * DOTS_RADIUD, 2 * DOTS_RADIUD);
            rel.addView(circleView);
            addView(rel, layoutParams);
        }
    }

    private void animateView() {
        animator = new ObjectAnimator[DOTS_COUNT];
        for (int i = 0; i < DOTS_COUNT; i++) {
            getChildAt(i).setTranslationY(getHeight() / MAX_JUMP);
            PropertyValuesHolder Y = PropertyValuesHolder.ofFloat(getChildAt(i).TRANSLATION_Y, -getHeight() / MAX_JUMP);
            PropertyValuesHolder X = PropertyValuesHolder.ofFloat(getChildAt(i).TRANSLATION_X, 0);
            animator[i] = ObjectAnimator.ofPropertyValuesHolder(getChildAt(i), X, Y);
            animator[i].setRepeatCount(-1);
            animator[i].setRepeatMode(ValueAnimator.REVERSE);
            animator[i].setDuration(DURATION);
            animator[i].setStartDelay((DURATION / DOTS_COUNT) * i);
            animator[i].start();
        }
    }
}
