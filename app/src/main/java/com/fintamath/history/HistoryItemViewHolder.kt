package com.fintamath.history

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import com.fintamath.R
import com.fintamath.mathview.MathTextView

class HistoryItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val mathTextView: MathTextView
    var removeButton: ImageButton
    var bookmarkButton: ToggleButton
    var calculateButton: ImageButton
    var dateTextView: TextView

    init {
        mathTextView = view.findViewById(R.id.historyMathTextView)
        removeButton = view.findViewById(R.id.historyDeleteButton)
        bookmarkButton = view.findViewById(R.id.historyBookmarkButton)
        calculateButton = view.findViewById(R.id.calculateButton)
        dateTextView = view.findViewById(R.id.dateTimeTextView)
    }
}
