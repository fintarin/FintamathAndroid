package com.fintamath.widget.graph

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class GridLine(
    private val width: Float,
    private val height: Float,
    private val x: Float,
    private val y: Float,
    private val isHorizontal: Boolean,
    private val value: Float
) {

    private val gridPaint = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 2f
    }

    private val textPaint = Paint().apply {
        color = Color.LTGRAY
        textSize = 25f
        textAlign = Paint.Align.RIGHT
    }

    fun onDraw(canvas: Canvas, cellSize: Float, drawInt : Boolean) {
        if (isHorizontal) {
            canvas.drawLine(0f, y, width, y, gridPaint)
            if (value != 0f)
                canvas.drawText(if (drawInt) value.toInt().toString() else value.toString(), (x - cellSize / 4).coerceIn(cellSize, width - cellSize / 4), y + cellSize / 4, textPaint)
        } else {
            canvas.drawLine(x, 0f, x, height, gridPaint)
            canvas.drawText(if (drawInt) value.toInt().toString() else value.toString(), x  - cellSize / 8, (y + cellSize / 2).coerceIn(cellSize / 2, height - cellSize / 4), textPaint)
        }
    }
}