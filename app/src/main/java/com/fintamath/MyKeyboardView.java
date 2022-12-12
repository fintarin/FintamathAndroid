package com.fintamath;

import android.content.Context;
import android.content.res.TypedArray;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyKeyboardView extends KeyboardView {

    private Map<Keyboard.Key,View> mMiniKeyboardCache;

    private MyKeyboardView mMiniKeyboard;
    private PopupWindow mPopupKeyboard;
    private int mPopupLayout;

    public MyKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mMiniKeyboardCache = new HashMap<>();

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
        mPopupX = mPopupX + popupKey.width - mMiniKeyboardContainer.getMeasuredWidth();
        mPopupY = mPopupY - mMiniKeyboardContainer.getMeasuredHeight();
        final int x = mPopupX + mMiniKeyboardContainer.getPaddingRight() + mCoordinates[0];
        final int y = mPopupY + mMiniKeyboardContainer.getPaddingBottom() + mCoordinates[1];

        mMiniKeyboard.setPopupOffset(x < 0 ? 0 : x, y);
        mMiniKeyboard.setShifted(isShifted());
        mPopupKeyboard.setContentView(mMiniKeyboardContainer);
        mPopupKeyboard.setWidth(mMiniKeyboardContainer.getMeasuredWidth());
        mPopupKeyboard.setHeight(mMiniKeyboardContainer.getMeasuredHeight());
        mPopupKeyboard.showAtLocation(this, Gravity.NO_GRAVITY, x, y);
        invalidateAllKeys();

        return true;
    }

    private void dismissPopupKeyboard() {
        if (mPopupKeyboard.isShowing()) {
            mPopupKeyboard.dismiss();
            invalidateAllKeys();
        }
    }
}
