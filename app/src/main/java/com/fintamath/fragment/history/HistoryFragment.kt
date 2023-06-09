package com.fintamath.fragment.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fintamath.R
import com.fintamath.storage.HistoryStorage
import com.fintamath.storage.MathTextData
import com.fintamath.storage.CalculatorInputStorage

class HistoryFragment : Fragment() {

    private lateinit var historyListView: RecyclerView
    private lateinit var emptyHistoryTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val fragmentView = inflater.inflate(R.layout.fragment_history, container, false)

        historyListView = fragmentView.findViewById(R.id.historyList)
        with(historyListView) {
            layoutManager = LinearLayoutManager(context)

            val historyAdapter = HistoryRecyclerViewAdapter()
            historyAdapter.onCalculate = { callOnCalculate(it) }
            historyAdapter.onItemsCountChange = { callOnItemsCountChange() }

            adapter = historyAdapter
        }

        emptyHistoryTextView = fragmentView.findViewById(R.id.emptyHistoryTextView)

        val historyBackButton: ImageButton = fragmentView.findViewById(R.id.historyBackButton)
        historyBackButton.setOnClickListener { executeBack() }

        return fragmentView
    }

    private fun executeBack() {
        activity?.onBackPressedDispatcher?.onBackPressed()
    }

    private fun callOnItemsCountChange() {
        val count = HistoryStorage.getHistoryList().size

        if (HistoryStorage.getHistoryList().size == 0 && emptyHistoryTextView.visibility != VISIBLE) {
            historyListView.visibility = GONE
            emptyHistoryTextView.visibility = VISIBLE
        } else if (count > 0 && emptyHistoryTextView.visibility != GONE) {
            emptyHistoryTextView.visibility = GONE
            historyListView.visibility = VISIBLE
        }
    }

    private fun callOnCalculate(text: String) {
        CalculatorInputStorage.mathTextData = MathTextData(text)
        executeBack()
    }
}