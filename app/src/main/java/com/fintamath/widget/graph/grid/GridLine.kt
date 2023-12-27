package com.fintamath.widget.graph.grid

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class GridLine(
    private val width: Float,
    private val height: Float,
    private val x: Float,
    private val y: Float,
    private val isHorizontal: Boolean
) {

    private val gridPaint = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 2f
    }

    fun onDraw(canvas: Canvas) {
        if (isHorizontal) {
            canvas.drawLine(0f, y, width, y, gridPaint)
        } else {
            canvas.drawLine(x, 0f, x, height, gridPaint)
        }
    }
}