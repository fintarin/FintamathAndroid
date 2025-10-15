package com.fintamath.widget.keyboard;

import static com.fintamath.utils.UtilsKt.getActivity;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

public class KeyboardPopupWindow {

    private final FrameLayout rootLayout;
    private View contentView;
    private boolean isShowing = false;

    public KeyboardPopupWindow(Context context) {
        Activity activity = getActivity(context);
        if (activity != null) {
            rootLayout = activity.getWindow().findViewById(android.R.id.content);
        }
        else {
            rootLayout = null;
        }
    }

    public void setContentView(View inContentView) {
        contentView = inContentView;
    }

    public void show(int x, int y, int width, int height) {
        if (rootLayout == null || contentView == null) {
            return;
        }

        var layoutParams = new FrameLayout.LayoutParams(width, height);
        layoutParams.leftMargin = x;
        layoutParams.topMargin = y;
        contentView.setLayoutParams(layoutParams);

        if (isShowing) {
            return;
        }

        rootLayout.addView(contentView);

        isShowing = true;
    }

    public void dismiss() {
        if (rootLayout == null) {
            return;
        }

        rootLayout.removeView(contentView);

        isShowing = false;
    }

    public boolean isShowing() {
        return isShowing;
    }
}
