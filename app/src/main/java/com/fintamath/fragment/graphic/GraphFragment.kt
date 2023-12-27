package com.fintamath.fragment.graphic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.fintamath.databinding.FragmentGraphicBinding

class GraphFragment : Fragment() {

    private lateinit var viewBinding: FragmentGraphicBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewBinding = FragmentGraphicBinding.inflate(inflater, container, false)

        viewBinding.aboutBackButton.setOnClickListener { executeBack() }

        return viewBinding.root
    }

    private fun executeBack() {
        viewBinding.root.findNavController().navigateUp()
    }
}
