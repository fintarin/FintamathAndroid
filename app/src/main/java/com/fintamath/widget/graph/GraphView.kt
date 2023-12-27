package com.fintamath.widget.graph

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.util.AttributeSet
import android.view.ScaleGestureDetector
import android.view.View
import com.fintamath.widget.graph.grid.GraphGrid
import kotlin.math.abs

class GraphView(
    ctx: Context,
    attrs: AttributeSet
) : View(ctx, attrs) {

    private var lockScrollScale = false


    private val axisPaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 3f
    }

    private var offsetX = 0.0f
    private var offsetY = 0.0f

    private val minCellCount: Int = 10
    private var currCellCount = minCellCount

    private var graphGrid: GraphGrid = GraphGrid()
    private var cellDelta = 1f


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        graphGrid.update(width, height, offsetX, offsetY, currCellCount, cellDelta)
        graphGrid.onDraw(canvas)

        if ((offsetX <= 0 && abs(offsetX) < (width / 2 - 40f)) || (offsetX > 0 && abs(offsetX) < (width / 2 - 20f))) {
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

    private var mScaleFactor = 1f

    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor /= detector.scaleFactor

            println(mScaleFactor)

            updateScale()


            invalidate()
            return true
        }
    }

    private fun updateScale() {
        if (mScaleFactor < 0.5f) {
            mScaleFactor = 1f
            cellDelta /=2
        }

        if (mScaleFactor > 1.5f) {
            mScaleFactor = 1f
            cellDelta *= 2

        }

        currCellCount = (minCellCount * mScaleFactor).toInt()
    }

    private val mScaleDetector = ScaleGestureDetector(context, scaleListener)


    private var lastX = 0f
    private var lastY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (lockScrollScale) {
            return true
        }
        if (mScaleDetector.onTouchEvent(event)) {
            return true
        }

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