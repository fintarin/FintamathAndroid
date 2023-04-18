package com.fintamath.fragment.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fintamath.R

class HistoryFragment : Fragment() {

    var onCalculate: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val fragmentView = inflater.inflate(R.layout.fragment_history, container, false)

        val historyList: RecyclerView = fragmentView.findViewById(R.id.historyList)
        with(historyList) {
            layoutManager = LinearLayoutManager(context)

            val historyAdapter = HistoryRecyclerViewAdapter()
            historyAdapter.onCalculate = { callOnCalculate(it) }

            adapter = historyAdapter
        }

        val historyBackButton: ImageButton = fragmentView.findViewById(R.id.historyBackButton)
        historyBackButton.setOnClickListener { executeBack() }

        return fragmentView
    }

    private fun executeBack() {
        activity?.onBackPressed()
    }

    private fun callOnCalculate(text: String) {
        onCalculate?.invoke(text)
        executeBack()
    }
}