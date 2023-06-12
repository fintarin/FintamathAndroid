package com.fintamath.fragment.camera

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.fintamath.MainActivity
import com.fintamath.R
import com.fintamath.databinding.FragmentCameraBinding
import com.fintamath.widget.fragment.BorderlessFragment
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraFragment : BorderlessFragment() {
    private lateinit var viewBinding: FragmentCameraBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraId: String
    private lateinit var camera: Camera
    private var isTurnFlashLight: Boolean = false
    private lateinit var zoomBar: SeekBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentCameraBinding.inflate(layoutInflater, container, false)

        viewBinding.cameraBackButton.setOnClickListener { executeBack() }
        viewBinding.cameraHistoryButton.setOnClickListener {
            viewBinding.root.findNavController().navigate(R.id.action_cameraFragment_to_historyFragment)
        }
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }
        viewBinding.flashLightButton.setOnClickListener { turnLight() }
        viewBinding.ImageFromGallery.setOnClickListener { takeFromGallery() }

        zoomBar = viewBinding.seekBar

        zoomBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                camera.cameraControl.setLinearZoom(progress / 100.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        return viewBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        cameraExecutor.shutdown()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (areAllPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (areAllPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(context,
                    R.string.no_permission,
                    Toast.LENGTH_SHORT).show()

                activity?.finish()
            }
        }
    }

    private fun startCamera()  {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setTargetRotation(viewBinding.viewFinder.display.rotation)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()

            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {

                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(context,
                        R.string.camera_failed,
                        Toast.LENGTH_SHORT).show()
                }

                @ExperimentalGetImage
                override fun onCaptureSuccess(imageProxy: ImageProxy){
                    val image = imageProxy.image

                    val buffer = image!!.planes[0].buffer
                    buffer.rewind()

                    val bytes = ByteArray(buffer.capacity())
                    buffer.get(bytes)

                    var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    bitmap = rotateImage(bitmap, imageProxy.imageInfo.rotationDegrees)

                    (activity as MainActivity).set_full_image(bitmap) // TODO! move it to storage

                    val location = IntArray(2)
                    viewBinding.focusExpr.getLocationOnScreen(location)

                    val cutBitmap = Bitmap.createBitmap(
                        bitmap,
                        location[0],
                        location[1],
                        viewBinding.focusExpr.width,
                        viewBinding.focusExpr.height
                    )
                    (activity as MainActivity).set_cut_image(cutBitmap) // TODO! move it to storage

                    imageProxy.close()
                    viewBinding.root.findNavController().navigate(
                        R.id.action_cameraFragment_to_recognitionFragment)
                }
            }
        )
    }

    private fun takeFromGallery(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, 10)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val image_uri: Uri
        if (requestCode == 10 && resultCode == RESULT_OK && data != null) {
            image_uri = data.data!!
            val location = IntArray(2)
            viewBinding.focusExpr.getLocationOnScreen(location)
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), image_uri)
            (activity as MainActivity).set_full_image(bitmap)
            val cut = Bitmap.createBitmap(bitmap, location[0]+20, location[1]+20, viewBinding.focusExpr.getWidth()-40,viewBinding.focusExpr.getHeight()-40)
            (activity as MainActivity).set_cut_image(cut)
            viewBinding.root.findNavController().navigate(R.id.action_cameraFragment_to_recognitionFragment)
        }
    }

    private fun turnLight(){
        camera.cameraControl.enableTorch(! isTurnFlashLight)
        isTurnFlashLight = ! isTurnFlashLight
    }


    private fun areAllPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun rotateImage(img: Bitmap, degree: Int): Bitmap? {
        if (degree == 0) {
            return img
        }

        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())

        val rotatedImg =
            Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)

        img.recycle()

        return rotatedImg
    }

    private fun executeBack() {
        activity?.onBackPressedDispatcher?.onBackPressed()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf (Manifest.permission.CAMERA).toTypedArray()
    }

}


