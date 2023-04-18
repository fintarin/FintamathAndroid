package com.fintamath.history

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.fintamath.R

class HistoryRecyclerViewAdapter() : RecyclerView.Adapter<HistoryItemViewHolder>() {

    var onCalculate: ((String) -> Unit)? = null

    init {
        HistoryStorage.onItemRemoved = { notifyItemRemoved(it) }
        HistoryStorage.onItemInserted = { notifyItemInserted(it) }
    }

    override fun getItemCount() = HistoryStorage.getList().size

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): HistoryItemViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.history_item, viewGroup, false)
        return HistoryItemViewHolder(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(viewHolder: HistoryItemViewHolder, position: Int) {
        viewHolder.removeButton.setOnClickListener { _ ->
            callOnRemoveButtonClicked(viewHolder)
        }
        viewHolder.bookmarkButton.setOnCheckedChangeListener { _, isChecked ->
            callOnBookmarkButtonCheckedChangeListener(viewHolder, isChecked)
        }
        viewHolder.calculateButton.setOnClickListener { _ ->
            onCalculate?.invoke(viewHolder.mathTextView.text)
        }
    }

    override fun onViewAttachedToWindow(viewHolder: HistoryItemViewHolder) {
        val historyList = HistoryStorage.getList()
        viewHolder.mathTextView.text = historyList[viewHolder.adapterPosition].text
        viewHolder.bookmarkButton.isChecked = historyList[viewHolder.adapterPosition].isBookmarked
    }

    private fun callOnBookmarkButtonCheckedChangeListener(viewHolder: HistoryItemViewHolder, isChecked: Boolean) {
        viewHolder.removeButton.visibility = if (isChecked) GONE else VISIBLE
        val index = viewHolder.adapterPosition
        HistoryStorage.setIsBookmarked(index, isChecked)
    }

    private fun callOnRemoveButtonClicked(viewHolder: HistoryItemViewHolder) {
        val index = viewHolder.adapterPosition
        HistoryStorage.remove(index)
    }
}
