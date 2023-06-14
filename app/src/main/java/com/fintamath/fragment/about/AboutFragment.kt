package com.fintamath.fragment.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fintamath.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private lateinit var viewBinding: FragmentAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewBinding = FragmentAboutBinding.inflate(inflater, container, false)

        viewBinding.aboutBackButton.setOnClickListener { executeBack() }

        return viewBinding.root
    }

    private fun executeBack() {
        activity?.onBackPressedDispatcher?.onBackPressed()
    }
}