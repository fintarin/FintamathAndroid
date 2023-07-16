package com.fintamath.fragment.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.fintamath.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var viewBinding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewBinding = FragmentSettingsBinding.inflate(inflater, container, false)

        viewBinding.settingsBackButton.setOnClickListener { executeBack() }

        return viewBinding.root
    }

    private fun executeBack() {
        viewBinding.root.findNavController().currentBackStack.value.first()
    }
}
