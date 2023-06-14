package com.fintamath.fragment.history

import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import com.fintamath.R
import com.fintamath.widget.mathview.MathTextView

internal class HistoryItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val layout: ViewGroup
    val mathTextView: MathTextView
    var removeButton: ImageButton
    var bookmarkButton: ToggleButton
    var dateTextView: TextView

    init {
        layout = view.findViewById(R.id.historyItemLayout)
        mathTextView = view.findViewById(R.id.historyMathTextView)
        removeButton = view.findViewById(R.id.historyDeleteButton)
        bookmarkButton = view.findViewById(R.id.historyBookmarkButton)
        dateTextView = view.findViewById(R.id.dateTimeTextView)
    }
}
