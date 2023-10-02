package com.fintamath.fragment.history

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
import java.util.concurrent.atomic.AtomicInteger

internal class HistoryRecyclerViewAdapter : RecyclerView.Adapter<HistoryItemViewHolder>() {

    var onLoaded: (() -> Unit)? = null
    var onItemsCountChange: ((Int) -> Unit)? = null
    var onCalculate: ((String) -> Unit)? = null

    private var recyclerView: RecyclerView? = null

    private var itemsToLoadNum = AtomicInteger(0)

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

            if (HistoryStorage.getItems()[it].isBookmarked) {
                recyclerView?.smoothScrollToPosition(it)
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        this.recyclerView = recyclerView

        if (itemCount == 0) {
            invokeOnLoaded()
        }
    }

    override fun getItemCount() = HistoryStorage.getItems().size

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): HistoryItemViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.layout_history_item, viewGroup, false)
        return HistoryItemViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: HistoryItemViewHolder, position: Int) {
        if (itemsToLoadNum.get() != -1) {
            itemsToLoadNum.incrementAndGet()

            viewHolder.mathTextView.setOnTextChangedListener { _, _ ->
                if (itemsToLoadNum.get() == -1) {
                    return@setOnTextChangedListener
                }

                itemsToLoadNum.decrementAndGet()

                if (itemsToLoadNum.get() == 0) {
                    invokeOnLoaded()
                }
            }
        }

        viewHolder.removeButton.setOnClickListener {
            onRemoveButtonClicked(viewHolder)
        }
        viewHolder.bookmarkButton.setOnCheckedChangeListener { _, isChecked ->
            onBookmarkButtonCheckedChangeListener(viewHolder, isChecked)
        }
        viewHolder.calculateButton.setOnClickListener {
            onCalculate?.invoke(viewHolder.mathTextView.text)
        }
    }

    override fun onViewAttachedToWindow(viewHolder: HistoryItemViewHolder) {
        val historyList = HistoryStorage.getItems()
        viewHolder.mathTextView.text = historyList[viewHolder.absoluteAdapterPosition].mathTextData.text
        viewHolder.bookmarkButton.isChecked = historyList[viewHolder.absoluteAdapterPosition].isBookmarked
        viewHolder.dateTextView.text = formatDataTime(historyList[viewHolder.absoluteAdapterPosition].dateTimeString)
    }

    private fun onBookmarkButtonCheckedChangeListener(viewHolder: HistoryItemViewHolder, isChecked: Boolean) {
        viewHolder.removeButton.visibility = if (isChecked) GONE else VISIBLE
        val index = viewHolder.absoluteAdapterPosition
        HistoryStorage.bookmarkItem(index, isChecked)
    }

    private fun onRemoveButtonClicked(viewHolder: HistoryItemViewHolder) {
        val index = viewHolder.absoluteAdapterPosition
        HistoryStorage.removeItem(index)
    }

    private fun invokeOnLoaded() {
        onLoaded?.invoke()
        itemsToLoadNum.set(-1)
    }

    private fun formatDataTime(dataTimeString: String): String {
        val dataTime = LocalDateTime.parse(dataTimeString)
        return dataTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
    }
}
