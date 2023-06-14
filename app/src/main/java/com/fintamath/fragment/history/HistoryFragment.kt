package com.fintamath.fragment.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.fintamath.databinding.FragmentHistoryBinding
import com.fintamath.storage.HistoryStorage
import com.fintamath.storage.MathTextData
import com.fintamath.storage.CalculatorInputStorage

class HistoryFragment : Fragment() {

    private lateinit var viewBinding: FragmentHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewBinding = FragmentHistoryBinding.inflate(inflater, container, false)

        with(viewBinding.historyListView) {
            layoutManager = LinearLayoutManager(context)

            val historyAdapter = HistoryRecyclerViewAdapter()
            historyAdapter.onItemClick = { onItemClick(it) }
            historyAdapter.onItemsCountChange = { onItemsCountChange() }

            adapter = historyAdapter
        }

        viewBinding.historyBackButton.setOnClickListener { executeBack() }

        return viewBinding.root
    }

    private fun executeBack() {
        activity?.onBackPressedDispatcher?.onBackPressed()
    }

    private fun onItemsCountChange() {
        val count = HistoryStorage.getItems().size

        if (HistoryStorage.getItems().size == 0 && viewBinding.emptyHistoryTextView.visibility != VISIBLE) {
            viewBinding.historyListView.visibility = GONE
            viewBinding.emptyHistoryTextView.visibility = VISIBLE
        } else if (count > 0 && viewBinding.emptyHistoryTextView.visibility != GONE) {
            viewBinding.emptyHistoryTextView.visibility = GONE
            viewBinding.historyListView.visibility = VISIBLE
        }
    }

    private fun onItemClick(text: String) {
        CalculatorInputStorage.mathTextData = MathTextData(text)
        executeBack()
    }
}
