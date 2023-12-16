package com.fintamath.fragment.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.fintamath.databinding.FragmentSettingsBinding
import com.fintamath.storage.SettingsStorage
import com.google.android.material.slider.Slider

class SettingsFragment : Fragment() {

    private lateinit var viewBinding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewBinding = FragmentSettingsBinding.inflate(inflater, container, false)

        viewBinding.settingsBackButton.setOnClickListener { executeBack() }

        if (SettingsStorage.getPrecision() <= viewBinding.precisionSlider.valueTo) {
            viewBinding.precisionSlider.value = SettingsStorage.getPrecision().toFloat()
        }
        else {
            SettingsStorage.setPrecision(viewBinding.precisionSlider.value.toInt())
        }

        viewBinding.precisionSlider.addOnChangeListener(Slider.OnChangeListener { _, value, _ -> onPrecisionChanged(value.toInt()) })

        return viewBinding.root
    }

    private fun onPrecisionChanged(precision: Int) {
        SettingsStorage.setPrecision(precision)
    }

    private fun executeBack() {
        viewBinding.root.findNavController().navigateUp()
    }
}
