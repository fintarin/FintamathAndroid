package com.fintamath.keyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;

import com.fintamath.R;

import java.util.HashMap;
import java.util.Map;

public class MyKeyboardView extends KeyboardView {

    private final PopupWindow mPopupKeyboard;
    private MyKeyboardView mMiniKeyboard;
    private int mPopupLayout;
    private boolean mMiniKeyboardOnScreen;
    private boolean mIsMiniKeyboard;
    private float mMiniKeyboardOffsetX;
    private float mMiniKeyboardOffsetY;
    private final Map<Keyboard.Key,View> mMiniKeyboardCache = new HashMap<>();

    public MyKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPopupKeyboard = new PopupWindow(context);
        mPopupKeyboard.setBackgroundDrawable(null);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.KeyboardView);
        int n = a.getIndexCount();

        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);

            if (attr == R.styleable.KeyboardView_popupLayout) {
                mPopupLayout = a.getResourceId(attr, 0);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mIsMiniKeyboard) {
            onTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_DOWN, 0, 0, 0));
        }
    }

    @Override
    public void setPopupOffset(int x, int y) {
        super.setPopupOffset(x, y);
        mMiniKeyboardOffsetX = x;
        mMiniKeyboardOffsetY = y;
        mIsMiniKeyboard = true;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent me) {
        if (mMiniKeyboardOnScreen && mMiniKeyboard != null && mMiniKeyboard.isAttachedToWindow()) {
            float x = me.getX() - mMiniKeyboard.mMiniKeyboardOffsetX;
            float y = mMiniKeyboard.getY();

            x = x >= mMiniKeyboard.getRight() ? mMiniKeyboard.getRight() - 1 : x;
            x = x <= mMiniKeyboard.getLeft() ? mMiniKeyboard.getLeft() + 1 : x;

            int action = me.getAction() == MotionEvent.ACTION_MOVE ? MotionEvent.ACTION_DOWN : me.getAction();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
                return mMiniKeyboard.onTouchEvent(MotionEvent.obtain(me.getDownTime(), me.getEventTime(),
                        action, x, y, me.getMetaState()));
            }

            return false;
        }

        return super.onTouchEvent(me);
    }

    @Override
    protected boolean onLongPress(Keyboard.Key popupKey) {
        int popupKeyboardId = popupKey.popupResId;

        if (popupKeyboardId == 0) {
            return false;
        }

        View mMiniKeyboardContainer = mMiniKeyboardCache.get(popupKey);
        if (mMiniKeyboardContainer == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mMiniKeyboardContainer = inflater.inflate(mPopupLayout, null);
            mMiniKeyboard = (MyKeyboardView) mMiniKeyboardContainer.findViewById(
                    R.id.keyboardView);

            mMiniKeyboard.setOnKeyboardActionListener(new OnKeyboardActionListener() {
                public void onKey(int primaryCode, int[] keyCodes) {
                    getOnKeyboardActionListener().onKey(primaryCode, keyCodes);
                    dismissPopupKeyboard();
                }

                public void onText(CharSequence text) {
                    getOnKeyboardActionListener().onText(text);
                    dismissPopupKeyboard();
                }

                public void swipeLeft() { }

                public void swipeRight() { }

                public void swipeUp() { }

                public void swipeDown() { }

                public void onPress(int primaryCode) {
                    getOnKeyboardActionListener().onPress(primaryCode);
                }

                public void onRelease(int primaryCode) {
                    getOnKeyboardActionListener().onRelease(primaryCode);
                }
            });

            Keyboard keyboard = new Keyboard(getContext(), popupKeyboardId);
            mMiniKeyboard.setKeyboard(keyboard);
            mMiniKeyboard.setPopupParent(this);
            mMiniKeyboardContainer.measure(
                    MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));

            mMiniKeyboardCache.put(popupKey, mMiniKeyboardContainer);
        } else {
            mMiniKeyboard = (MyKeyboardView) mMiniKeyboardContainer.findViewById(R.id.keyboardView);
        }

        int[] mCoordinates = new int[2];
        getLocationInWindow(mCoordinates);
        int mPopupX = popupKey.x + getPaddingLeft();
        int mPopupY = popupKey.y + getPaddingTop();
        mPopupY = mPopupY - mMiniKeyboardContainer.getMeasuredHeight() / 2;
        final int x = mPopupX + mMiniKeyboardContainer.getPaddingRight() + mCoordinates[0];
        final int y = mPopupY + mMiniKeyboardContainer.getPaddingBottom() + mCoordinates[1];

        mMiniKeyboard.setPopupOffset(Math.max(x, 0), y);
        mMiniKeyboard.setShifted(isShifted());
        mPopupKeyboard.setContentView(mMiniKeyboardContainer);
        mPopupKeyboard.setWidth(mMiniKeyboardContainer.getMeasuredWidth());
        mPopupKeyboard.setHeight(mMiniKeyboardContainer.getMeasuredHeight());
        mPopupKeyboard.showAtLocation(this, Gravity.NO_GRAVITY, x, y);
        mMiniKeyboardOnScreen = true;
        invalidateAllKeys();

        return true;
    }

    private void dismissPopupKeyboard() {
        if (mPopupKeyboard.isShowing()) {
            mPopupKeyboard.dismiss();
            mMiniKeyboardOnScreen = false;
            invalidateAllKeys();
        }
    }
}
