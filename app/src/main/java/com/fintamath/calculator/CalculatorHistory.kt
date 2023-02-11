package com.fintamath.calculator

class CalculatorHistory {

    private var undoStack = ArrayDeque<String>()
    private var redoStack = ArrayDeque<String>()

    fun addState(state : String) {
        undoStack.addLast(state)
        redoStack.clear()
    }

    fun undo() : String {
        if (!undoStack.isEmpty()) {
            redoStack.addLast(undoStack.removeLast())
            return if (undoStack.isEmpty()) "" else undoStack.last()
        }
        return ""
    }

    fun redo() : String {
        if (!redoStack.isEmpty()) {
            val state = redoStack.removeLast()
            undoStack.addLast(state)
            return state
        }
        return if (undoStack.isEmpty()) "" else undoStack.last()
    }
}
