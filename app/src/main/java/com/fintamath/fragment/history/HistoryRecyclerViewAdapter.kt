package com.fintamath.fragment.history

import android.view.LayoutInflater
import android.view.MotionEvent
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

    var recyclerView: RecyclerView? = null

    var onItemsCountChange: ((Int) -> Unit)? = null
    var onItemClick: ((String) -> Unit)? = null

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

            if (HistoryStorage.getHistoryList()[it].isBookmarked) {
                recyclerView?.smoothScrollToPosition(it)
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView
    }

    override fun getItemCount() = HistoryStorage.getHistoryList().size

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): HistoryItemViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.layout_history_item, viewGroup, false)
        return HistoryItemViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: HistoryItemViewHolder, position: Int) {
        viewHolder.removeButton.setOnClickListener {
            onRemoveButtonClicked(viewHolder)
        }
        viewHolder.bookmarkButton.setOnCheckedChangeListener { _, isChecked ->
            onBookmarkButtonCheckedChangeListener(viewHolder, isChecked)
        }
        viewHolder.layout.setOnClickListener {
            onItemClick?.invoke(viewHolder.mathTextView.text)
        }
    }

    override fun onViewAttachedToWindow(viewHolder: HistoryItemViewHolder) {
        val historyList = HistoryStorage.getHistoryList()
        viewHolder.mathTextView.text = historyList[viewHolder.absoluteAdapterPosition].mathTextData.text
        viewHolder.bookmarkButton.isChecked = historyList[viewHolder.absoluteAdapterPosition].isBookmarked
        viewHolder.dateTextView.text = formatDataTime(historyList[viewHolder.absoluteAdapterPosition].dateTimeString)
    }

    private fun onBookmarkButtonCheckedChangeListener(viewHolder: HistoryItemViewHolder, isChecked: Boolean) {
        viewHolder.removeButton.visibility = if (isChecked) GONE else VISIBLE
        val index = viewHolder.absoluteAdapterPosition
        HistoryStorage.bookmarkItem(index, isChecked)
    }

    private fun onMathTextClicked(viewHolder: HistoryItemViewHolder, event: MotionEvent): Boolean {
        return viewHolder.layout.onTouchEvent(event)
    }

    private fun onRemoveButtonClicked(viewHolder: HistoryItemViewHolder) {
        val index = viewHolder.absoluteAdapterPosition
        HistoryStorage.removeItem(index)
    }

    private fun formatDataTime(dataTimeString: String): String {
        val dataTime = LocalDateTime.parse(dataTimeString)
        return dataTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
    }
}
