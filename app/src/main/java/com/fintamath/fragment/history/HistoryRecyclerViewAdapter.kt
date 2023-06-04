package com.fintamath.fragment.history

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fintamath.R
import com.fintamath.storage.HistoryStorage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal class HistoryRecyclerViewAdapter : RecyclerView.Adapter<HistoryItemViewHolder>() {

    var onItemsCountChange: ((Int) -> Unit)? = null
    var onCalculate: ((String) -> Unit)? = null

    init {
        HistoryStorage.onItemsLoaded = { start, end ->
            notifyItemRangeChanged(start, end)
            onItemsCountChange?.invoke(itemCount)
        }
        HistoryStorage.onItemRemoved = {
            notifyItemRemoved(it)
            onItemsCountChange?.invoke(itemCount)
        }
        HistoryStorage.onItemInserted = {
            notifyItemInserted(it)
            onItemsCountChange?.invoke(itemCount)
        }
    }

    override fun getItemCount() = HistoryStorage.getHistoryList().size

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
        val historyList = HistoryStorage.getHistoryList()
        viewHolder.mathTextView.text = historyList[viewHolder.absoluteAdapterPosition].mathTextData.text
        viewHolder.bookmarkButton.isChecked = historyList[viewHolder.absoluteAdapterPosition].isBookmarked
        viewHolder.dateTextView.text = formatDataTime(historyList[viewHolder.absoluteAdapterPosition].dateTimeString)
    }

    private fun callOnBookmarkButtonCheckedChangeListener(viewHolder: HistoryItemViewHolder, isChecked: Boolean) {
        viewHolder.removeButton.visibility = if (isChecked) GONE else VISIBLE
        val index = viewHolder.absoluteAdapterPosition
        HistoryStorage.bookmarkItem(index, isChecked)
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
