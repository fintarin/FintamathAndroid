package com.fintamath.loading;

import static com.fintamath.loading.LoadingConstant.DEFAULT_COLOR;
import static com.fintamath.loading.LoadingConstant.DEFAULT_DOTS_COUNT;
import static com.fintamath.loading.LoadingConstant.DEFAULT_DOTS_RADIUS;
import static com.fintamath.loading.LoadingConstant.DEFAULT_DURATION;
import static com.fintamath.loading.LoadingConstant.MAX_JUMP;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.fintamath.R;


public class LoadingView extends BaseLinearLoading {

    private int DOTS_RADIUD = DEFAULT_DOTS_RADIUS;
    private int DOTS_COUNT = DEFAULT_DOTS_COUNT;
    private int COLOR = DEFAULT_COLOR;
    private int DURATION = DEFAULT_DURATION;

    private ObjectAnimator animator[];
    boolean onLayoutReach = false;

    public LoadingView(Context context) {
        super(context);
        initView(context, null, DEFAULT_DOTS_RADIUS,DEFAULT_DOTS_COUNT, DEFAULT_COLOR);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs, DEFAULT_DOTS_RADIUS,DEFAULT_DOTS_COUNT, DEFAULT_COLOR);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, DEFAULT_DOTS_RADIUS,DEFAULT_DOTS_COUNT, DEFAULT_COLOR);
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

    @Override
    protected void initView(Context context, @Nullable AttributeSet attrs, int dotsSize, int dotsCount, int color) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadingView);

        this.COLOR = a.getColor(R.styleable.LoadingView_dots_color, DEFAULT_COLOR);
        this.DURATION = a.getInt(R.styleable.LoadingView_dots_duration, DEFAULT_DURATION);
        this.DOTS_COUNT = a.getInt(R.styleable.LoadingView_dots_count, DEFAULT_DOTS_COUNT);
        this.DOTS_RADIUD = a.getDimensionPixelSize(R.styleable.LoadingView_dots_radius, DEFAULT_DOTS_RADIUS);

        a.recycle();

        super.initView(context, attrs, DOTS_RADIUD,DOTS_COUNT, COLOR);
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
