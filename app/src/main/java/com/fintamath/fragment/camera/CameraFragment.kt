package com.fintamath.fragment.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fintamath.databinding.FragmentCameraBinding

class CameraFragment : Fragment() {

    private lateinit var viewBinding: FragmentCameraBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewBinding = FragmentCameraBinding.inflate(inflater, container, false)

        viewBinding.cameraBackButton.setOnClickListener { executeBack() }

        return viewBinding.root
    }

    private fun executeBack() {
        activity?.onBackPressedDispatcher?.onBackPressed()
    }
}
