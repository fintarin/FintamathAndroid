package com.fintamath.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fintamath.R

class HistoryFragment : Fragment() {

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
            adapter = HistoryRecyclerViewAdapter(arrayOf(
                "sin(x)^2 + cos(x)^2",
                "abc",
                "(a+1)^2",
                "x^2 + 2x - 1 = 0",
                "sqrt(1/2)",
                "ln(tan(x^2)/cot(x^2))",
                "sqrt((a+b)^2 / (a+c * (1/2)))",
                "sqrt((a+b)^2 / (a+c * (1/2)))",
                "sqrt((a+b)^2 / (a+c * (1/2)))",
                "sqrt((a+b)^2 / (a+c * (1/2)))",
                "sqrt((a+b)^2 / (a+c * (1/2)))",
                "sqrt((a+b)^2 / (a+c * (1/2)))",
                "sqrt((a+b)^2 / (a+c * (1/2)))",
                "sqrt((a+b)^2 / (a+c * (1/2))) aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa aaaaaaaaaaaaa sqrt(3)",
            ))
        }

        val historyBackButton: ImageButton = fragmentView.findViewById(R.id.historyBackButton)
        historyBackButton.setOnClickListener { executeBack() }

        return fragmentView
    }

    private fun executeBack() {
        activity?.onBackPressed()
    }
}