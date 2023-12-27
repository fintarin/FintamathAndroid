package com.fintamath.fragment.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fintamath.R
import com.fintamath.databinding.FragmentHistoryBinding
import com.fintamath.storage.HistoryStorage
import com.fintamath.storage.MathTextData
import com.fintamath.storage.CalculatorStorage

class HistoryFragment : Fragment() {

    private lateinit var viewBinding: FragmentHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewBinding = FragmentHistoryBinding.inflate(inflater, container, false)

        initBarButtons()

        viewBinding.loadingView.postOnAnimation {
            with(viewBinding.historyListView) {
                layoutManager = LinearLayoutManager(context)

                val historyAdapter = HistoryRecyclerViewAdapter()
                historyAdapter.onLoaded = { onHistoryLoaded() }
                historyAdapter.onCalculate = { onCalculate(it) }
                historyAdapter.onItemsCountChange = { onItemsCountChange() }

                adapter = historyAdapter
            }
        }

        return viewBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun initBarButtons() {
        viewBinding.calculatorButton.setOnClickListener { showCalculatorFragment() }
        viewBinding.graphButton.setOnClickListener { showGraphFragment() }
        // viewBinding.cameraButton.setOnClickListener { showCameraFragment() } // TODO: uncomment when camera is implemented{
        viewBinding.settingsButton.setOnClickListener { showSettingsFragment() }
        viewBinding.aboutButton.setOnClickListener { showAboutFragment() }
    }

    private fun onItemsCountChange() {
        if (HistoryStorage.getItems().isEmpty()) {
            viewBinding.historyListView.visibility = GONE
            viewBinding.emptyHistoryTextView.visibility = VISIBLE
        } else {
            viewBinding.emptyHistoryTextView.visibility = GONE
            viewBinding.historyListView.visibility = VISIBLE
        }
    }

    private fun onHistoryLoaded() {
        viewBinding.loadingView.visibility = GONE
        onItemsCountChange()
    }

    private fun onCalculate(text: String) {
        CalculatorStorage.inputMathTextData = MathTextData(text)
        showCalculatorFragment()
    }

    private fun showCalculatorFragment() {
        executeBack()
    }

    private fun showGraphFragment() {
        executeBack()
        showFragment(R.id.action_calculatorFragment_to_graphFragment)
    }

    private fun showCameraFragment() {
        executeBack()
        showFragment(R.id.action_calculatorFragment_to_cameraFragment)
    }

    private fun showAboutFragment() {
        showFragment(R.id.action_historyFragment_to_aboutFragment)
    }

    private fun showSettingsFragment() {
        showFragment(R.id.action_historyFragment_to_settingsFragment)
    }

    private fun showFragment(navigationId: Int) {
        viewBinding.root.findNavController().navigate(navigationId)
    }

    private fun executeBack() {
        viewBinding.root.findNavController().navigateUp()
    }
}