package com.fintamath.history

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.fintamath.R
import com.fintamath.textview.MathTextView

class HistoryItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val mathTextView: MathTextView

    init {
        mathTextView = view.findViewById(R.id.historyMathTextView)
    }
}
