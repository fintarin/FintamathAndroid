package com.fintamath.widget.graph

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.ScaleGestureDetector
import android.view.View
import java.math.BigDecimal
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class GraphView(
    ctx: Context,
    attrs: AttributeSet
) : View(ctx, attrs) {

    private var lockScrollScale = false

    private var points : MutableMap<BigDecimal, BigDecimal> = mutableMapOf()

    private val axisPaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 3f
    }

    private val pointPaint = Paint().apply {
        color = Color.CYAN
        strokeWidth = 4f
    }

    private var offsetX = 0.0f
    private var offsetY = 0.0f

    private val minCellCount: Int = 10
    private var currCellCount = minCellCount

    private var updateLambda: ()->Unit = {}

    private var graphGrid: GraphGrid = GraphGrid()
    private var cellDelta = 1f
    private var scaleFactor = 1f

    private val scrollDetector = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {

            if (e1 == null) {
                return false
            }

            when (e2.action) {
                MotionEvent.ACTION_MOVE -> {
                    offsetX -= distanceX
                    offsetY -= distanceY
                }
            }

            onScrollOrScale()
            invalidate()

            return true
        }
    })

    private val scaleDetector = ScaleGestureDetector(ctx, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor /= detector.scaleFactor

            updateScale()
            onScrollOrScale()
            invalidate()

            return true
        }
    })

    init {
        setOnTouchListener {_, event ->
            scrollDetector.onTouchEvent(event)
            scaleDetector.onTouchEvent(event)
        }
    }

    fun setOnScrollOrScale(onScrollOnScale: ()->Unit) {
        updateLambda = onScrollOnScale
    }

    fun clearGraph() {
        points.clear()
    }

    fun addPoint(x: BigDecimal, y: BigDecimal) {
        points[x] = y
        invalidate()
    }

    fun hasPoint(x: BigDecimal): Boolean {
        return points.contains(x)
    }

    private fun onScrollOrScale() {
        updateLambda()
    }

    private fun updateScale() {
        if (scaleFactor < 0.5f) {
            scaleFactor = 1f
            cellDelta /=2
        }

        if (scaleFactor > 1.5f) {
            scaleFactor = 1f
            cellDelta *= 2

        }

        currCellCount = (minCellCount * scaleFactor).toInt()
    }

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

        drawPoints(canvas)
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

    private fun drawPoints(canvas: Canvas) {
        var oldX: BigDecimal? = null
        var oldY: BigDecimal? = null

        for (x in points.keys.toList().sorted()) {
            val y = points[x]

            if (oldX == null) {
                oldX = x
                oldY = y
                if (y != null) {
                    drawPointOnCanvas(x, y, canvas)
                }
                continue
            }
            if (y != null && oldY != null) {
                val delta = abs(oldY.toFloat() - y.toFloat()) * graphGrid.getCellSize() / cellDelta
                if (delta > height) {
                    oldX = x
                    oldY = y
                    continue
                }
                drawLineOnCanvas(x, y, oldX, oldY, canvas)
                oldX = x
                oldY = y
            }
        }
    }

    private fun drawPointOnCanvas(x: BigDecimal, y: BigDecimal, canvas: Canvas) {
        val cellSize = graphGrid.getCellSize()
        canvas.drawPoint(offsetX + width/2 + x.toFloat() * cellSize / cellDelta,
            offsetY + height/2 - y.toFloat() * cellSize / cellDelta, pointPaint)
    }

    private fun drawLineOnCanvas(x1: BigDecimal, y1:BigDecimal, x2:BigDecimal, y2:BigDecimal, canvas: Canvas) {
        val cellSize = graphGrid.getCellSize()
        canvas.drawLine(offsetX + width/2 + x1.toFloat() * cellSize / cellDelta,
            offsetY + height/2 - y1.toFloat() * cellSize / cellDelta,
            offsetX + width/2 + x2.toFloat() * cellSize / cellDelta,
            offsetY + height/2 - y2.toFloat() * cellSize / cellDelta,
            pointPaint)
    }

    fun getMinX(): BigDecimal {
        return canvasXToGraphX(BigDecimal(0))
    }

    fun getMaxX() : BigDecimal {
        return canvasXToGraphX(BigDecimal(width))
    }

    private fun canvasXToGraphX(canvasX: BigDecimal) : BigDecimal {
        return BigDecimal((cellDelta/graphGrid.getCellSize() * (canvasX.toFloat() - offsetX - width/2)).toString())
    }

    private fun graphXToCanvasX(graphX: BigDecimal) : BigDecimal{
        return BigDecimal((offsetX + width/2 + graphX.toFloat() * graphGrid.getCellSize() / cellDelta).toString())
    }

}