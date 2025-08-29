package com.fintamath.utils

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fintamath.R

fun addInsets(view: View) {
    ViewCompat.setOnApplyWindowInsetsListener(
        view,
        OnApplyWindowInsetsListener { v: View, insets: WindowInsetsCompat ->
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
    )
}
