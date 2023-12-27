package com.fintamath.widget.graph

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.math.min

class GraphView(
    ctx: Context,
    attrs: AttributeSet
) : View(ctx, attrs) {

    private val axisPaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 3f
    }

    private val gridPaint = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 2f
    }

    private var offsetX = 0.0f
    private var offsetY = 0.0f

    private var cellSize: Float = 0.0f
    private val minCellCount: Int = 10

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        cellSize = min(height, width) / minCellCount

        drawGrid(canvas)

        if (abs(offsetX) < width / 2) {
            drawVerticalAxis(canvas)
        }

        if (abs(offsetY) < height / 2) {
            drawHorizontalAxis(canvas)
        }

    }

    private fun drawHorizontalAxis(canvas: Canvas) {
        val offsetHeight = height.toFloat() / 2 + offsetY
        val width = width.toFloat()
        canvas.drawLine(0f, offsetHeight, width, offsetHeight, axisPaint)
    }

    private fun drawVerticalAxis(canvas: Canvas) {
        val height = height.toFloat()
        val offsetWidth = width.toFloat() / 2 + offsetX
        canvas.drawLine(offsetWidth, 0f, offsetWidth, height, axisPaint)
    }

    private var lastX = 0f
    private var lastY = 0f

    private fun drawGrid(canvas: Canvas) {
        var currentXCoord = offsetX % cellSize
        var currentYCoord = offsetY % cellSize

        while (currentXCoord < width.toFloat()) {
            canvas.drawLine(currentXCoord, 0.0f, currentXCoord, height.toFloat(), gridPaint)
            currentXCoord += cellSize
        }

        while (currentYCoord < height.toFloat()) {
            canvas.drawLine(0.0f, currentYCoord, width.toFloat(), currentYCoord, gridPaint)
            currentYCoord += cellSize
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = x
                lastY = y
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = x - lastX
                val deltaY = y - lastY

                offsetX += deltaX
                offsetY += deltaY

                lastX = x
                lastY = y
            }
        }
        invalidate()
        return true
    }
}