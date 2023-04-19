package com.fintamath.fragment.history

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.fintamath.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class HistoryRecyclerViewAdapter : RecyclerView.Adapter<HistoryItemViewHolder>() {

    var onCalculate: ((String) -> Unit)? = null

    init {
        HistoryStorage.onItemsLoaded = { start, end -> notifyItemRangeChanged(start, end) }
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
        viewHolder.removeButton.setOnClickListener {
            callOnRemoveButtonClicked(viewHolder)
        }
        viewHolder.bookmarkButton.setOnCheckedChangeListener { _, isChecked ->
            callOnBookmarkButtonCheckedChangeListener(viewHolder, isChecked)
        }
        viewHolder.calculateButton.setOnClickListener {
            onCalculate?.invoke(viewHolder.mathTextView.text)
        }
    }

    override fun onViewAttachedToWindow(viewHolder: HistoryItemViewHolder) {
        val historyList = HistoryStorage.getList()
        viewHolder.mathTextView.text = historyList[viewHolder.absoluteAdapterPosition].text
        viewHolder.bookmarkButton.isChecked = historyList[viewHolder.absoluteAdapterPosition].isBookmarked
        viewHolder.dateTextView.text = formatDataTime(historyList[viewHolder.absoluteAdapterPosition].dateTimeString)
    }

    private fun callOnBookmarkButtonCheckedChangeListener(viewHolder: HistoryItemViewHolder, isChecked: Boolean) {
        viewHolder.removeButton.visibility = if (isChecked) GONE else VISIBLE
        val index = viewHolder.absoluteAdapterPosition
        HistoryStorage.setItemIsBookmarked(index, isChecked)
    }

    private fun callOnRemoveButtonClicked(viewHolder: HistoryItemViewHolder) {
        val index = viewHolder.absoluteAdapterPosition
        HistoryStorage.removeItem(index)
    }

    private fun formatDataTime(dataTimeString: String): String {
        val dataTime = LocalDateTime.parse(dataTimeString)
        return dataTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
    }
}
