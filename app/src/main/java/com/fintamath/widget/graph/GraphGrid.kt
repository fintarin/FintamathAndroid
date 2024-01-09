package com.fintamath.widget.graph

import android.graphics.Canvas
import kotlin.math.min

class GraphGrid() {

    private var offsetX: Float = 0f
    private var offsetY: Float = 0f

    private var width: Float = 0f
    private var height: Float = 0f

    private var gridLines : MutableList<GridLine> = ArrayList()

    private var cellSize: Float = 0.0f
    private var cellDelta: Float = 0.0f
    private var minimalX: Float = 0.0f
    private var minimalY: Float = 0.0f

    fun onDraw(canvas: Canvas) {
        for (gridLine in gridLines) {
            gridLine.onDraw(canvas, cellSize, cellDelta >= 1)
        }
    }

    fun update(width: Float,
               height: Float,
               offsetX: Float,
               offsetY: Float,
               cellCount: Int,
               cellDelta: Float) {

        this.gridLines = ArrayList()
        this.width = width
        this.height = height
        this.cellSize = min(this.height, this.width) / cellCount

        this.offsetX = offsetX
        this.offsetY = offsetY
        this.cellDelta = cellDelta
        this.minimalY = countMinimalY().toFloat()
        this.minimalX = countMinimalX().toFloat()

        addVerticalLines()
        addHorizontalLines()
    }

    fun getCellSize(): Float {
        return cellSize
    }

    private fun countMinimalX() : Int {
        return ((-width / 2 - offsetX) / cellSize).toInt()
    }

    private fun countMinimalY() : Int {
        return ((offsetY + height / 2 ) / cellSize).toInt()
    }

    private fun addVerticalLines() {
        var xCoord = (offsetX + width / 2) % cellSize
        var value: Float = minimalX * cellDelta
        while (xCoord < width) {
            gridLines.add(GridLine(width, height, xCoord, offsetY + height / 2, false, value))
            xCoord += cellSize
            value += cellDelta
        }
    }

    private fun addHorizontalLines() {
        var yCoord = (offsetY + height / 2) % cellSize
        var value: Float = minimalY * cellDelta
        while (yCoord < height) {
            gridLines.add(GridLine(width, height, offsetX + width / 2, yCoord, true, value))
            yCoord += cellSize
            value -= cellDelta
        }
    }

}