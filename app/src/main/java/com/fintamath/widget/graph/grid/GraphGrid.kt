package com.fintamath.widget.graph.grid

import android.graphics.Canvas
import kotlin.math.min

class GraphGrid() {

    private var offsetX: Float = 0f
    private var offsetY: Float = 0f

    private var width: Float = 0f
    private var height: Float = 0f

    private var gridLines : MutableList<GridLine> = ArrayList()

    private var cellSize: Float = 0.0f

    fun onDraw(canvas: Canvas) {
        for (gridLine in gridLines) {
            gridLine.onDraw(canvas)
        }
    }

    fun update(newWidth: Float, newHeight: Float, newOffsetX: Float, newOffsetY: Float, cellCount: Int) {
        gridLines = ArrayList()
        width = newWidth
        height = newHeight
        cellSize = min(height, width) / cellCount

        offsetX = (newOffsetX + width / 2) % cellSize
        offsetY = (newOffsetY + height / 2) % cellSize
        addVerticalLines()
        addHorizontalLines()
    }

    private fun addVerticalLines() {
        var xCoord = offsetX
        while (xCoord < width) {
            gridLines.add(GridLine(width, height, xCoord, 0f, false))
            xCoord += cellSize
        }
    }

    private fun addHorizontalLines() {
        var yCoord = offsetY
        while (yCoord < height) {
            gridLines.add(GridLine(width, height, 0f, yCoord, true))
            yCoord += cellSize
        }
    }

}