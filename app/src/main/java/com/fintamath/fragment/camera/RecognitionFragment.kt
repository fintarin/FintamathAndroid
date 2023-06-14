package com.fintamath.fragment.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fintamath.databinding.FragmentRecognitionBinding
import com.fintamath.widget.fragment.BorderlessFragment

class RecognitionFragment : BorderlessFragment() {
    private lateinit var viewBinding: FragmentRecognitionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentRecognitionBinding.inflate(layoutInflater, container, false)

        viewBinding.recBackButton.setOnClickListener { executeBack() }

        return viewBinding.root
    }

    private fun executeBack() {
        activity?.onBackPressedDispatcher?.onBackPressed()
    }
}
