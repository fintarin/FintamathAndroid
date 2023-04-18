package com.fintamath.widget.loading

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View

@SuppressLint("ViewConstructor")
class LoadingCircleView(
    context: Context?,
    var circleRadius: Int,
    var circleColor: Int,
    var isAntiAlias: Boolean
) : View(context) {

    var strokeWidth = 0

    var drawOnlyStroke = false

    private var xyCoordinates = 0.0f
    private val paint = Paint()

    init {
        paint.isAntiAlias = isAntiAlias

        if (drawOnlyStroke) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = strokeWidth.toFloat()
        } else {
            paint.style = Paint.Style.FILL
        }
        paint.color = circleColor

        //adding half of strokeWidth because
        //the stroke will be half inside the drawing circle and half outside
        xyCoordinates = circleRadius + (strokeWidth / 2).toFloat()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthHeight = 2 * circleRadius + strokeWidth
        setMeasuredDimension(widthHeight, widthHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(xyCoordinates, xyCoordinates, circleRadius.toFloat(), paint)
    }
}
