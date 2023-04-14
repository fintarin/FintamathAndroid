package com.fintamath.history

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.fintamath.R

class HistoryRecyclerViewAdapter(private val values: Array<String>) :
    RecyclerView.Adapter<HistoryItemViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): HistoryItemViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.history_item, viewGroup, false)
        return HistoryItemViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: HistoryItemViewHolder, position: Int) {
        viewHolder.mathTextView.text = values[position]
    }

    override fun getItemCount() = values.size
}
