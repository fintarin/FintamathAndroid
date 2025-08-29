package com.fintamath.fragment.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.fintamath.databinding.FragmentAboutBinding
import com.fintamath.utils.addInsets

class AboutFragment : Fragment() {

    private lateinit var viewBinding: FragmentAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewBinding = FragmentAboutBinding.inflate(inflater, container, false)

        viewBinding.aboutBackButton.setOnClickListener { executeBack() }

        addInsets(viewBinding.root)

        return viewBinding.root
    }

    private fun executeBack() {
        viewBinding.root.findNavController().navigateUp()
    }
}