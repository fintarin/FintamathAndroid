package com.fintamath.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fintamath.R

fun addInsets(view: View) {
    ViewCompat.setOnApplyWindowInsetsListener(
        view
    ) { v: View, insets: WindowInsetsCompat ->
        val systemInsets: Insets = insets.getInsets(
            WindowInsetsCompat.Type.systemBars()
        )
        v.setPadding(
            systemInsets.left,
            systemInsets.top,
            systemInsets.right,
            systemInsets.bottom
        )
        v.setBackgroundColor(ContextCompat.getColor(v.context, R.color.background_bar))
        insets
    }
}

fun getActivity(context: Context): Activity? {
    var activityContext: Context? = context

    while (activityContext is ContextWrapper) {
        if (activityContext is Activity) {
            return activityContext
        }

        activityContext = activityContext.baseContext
    }

    return null
}
