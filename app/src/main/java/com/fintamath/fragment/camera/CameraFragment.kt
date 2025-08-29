package com.fintamath.fragment.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.fintamath.R
import com.fintamath.databinding.FragmentCameraBinding
import com.fintamath.utils.addInsets

class CameraFragment : Fragment() {

    private lateinit var viewBinding: FragmentCameraBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewBinding = FragmentCameraBinding.inflate(inflater, container, false)

        initBarButtons()

        addInsets(viewBinding.root)

        return viewBinding.root
    }

    private fun initBarButtons() {
        viewBinding.calculatorButton.setOnClickListener { showCalculatorFragment() }
        viewBinding.historyButton.setOnClickListener { showHistoryFragment() }
        viewBinding.settingsButton.setOnClickListener { showSettingsFragment() }
        viewBinding.aboutButton.setOnClickListener { showAboutFragment() }
    }

    private fun showCalculatorFragment() {
        executeBack()
    }

    private fun showHistoryFragment() {
        executeBack()
        showFragment(R.id.action_calculatorFragment_to_historyFragment)
    }

    private fun showAboutFragment() {
        showFragment(R.id.action_cameraFragment_to_aboutFragment)
    }

    private fun showSettingsFragment() {
        showFragment(R.id.action_cameraFragment_to_settingsFragment)
    }

    private fun showFragment(navigationId: Int) {
        viewBinding.root.findNavController().navigate(navigationId)
    }

    private fun executeBack() {
        viewBinding.root.findNavController().navigateUp()
    }
}
