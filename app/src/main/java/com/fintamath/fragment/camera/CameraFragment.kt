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
import android.util.DisplayMetrics
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
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

    companion object {
        private const val CAMERA_REQUEST_CODE = 1
        private const val GALLERY_REQUEST_CODE = 2
        private val CAMERA_PERMISSIONS = mutableListOf(Manifest.permission.CAMERA).toTypedArray()
    }

    private lateinit var viewBinding: FragmentCameraBinding

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var imageCapture: ImageCapture
    private lateinit var camera: Camera

    private var isFlashLightOn = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentCameraBinding.inflate(layoutInflater, container, false)

        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }
        viewBinding.flashLightButton.setOnClickListener { toggleFlashLight() }
        viewBinding.ImageFromGallery.setOnClickListener { takeFromGallery() }
        viewBinding.cameraBackButton.setOnClickListener { executeBack() }
        viewBinding.cameraHistoryButton.setOnClickListener {
            viewBinding.root.findNavController().navigate(R.id.action_cameraFragment_to_historyFragment)
        }

        viewBinding.zoomBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                camera.cameraControl.setLinearZoom(progress / 100.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arePermissionsGranted()) {
            initCamera()
        } else {
            requestPermissions(CAMERA_PERMISSIONS, CAMERA_REQUEST_CODE)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (arePermissionsGranted()) {
                initCamera()
            } else {
                Toast.makeText(context,
                    R.string.no_permission,
                    Toast.LENGTH_SHORT).show()

                executeBack()
            }
        }
    }

    private fun initCamera()  {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setTargetRotation(viewBinding.viewFinder.display.rotation)
                .setTargetResolution(getDisplaySize())
                .setCaptureMode(CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

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

                    var screenImg = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    screenImg = rotateImage(screenImg, imageProxy.imageInfo.rotationDegrees)
                    screenImg = scaleImageToDisplaySize(screenImg)

                    (activity as MainActivity).setScreenImage(screenImg) // TODO! move it to storage

                    val location = IntArray(2)
                    viewBinding.recognitionSquare.getLocationOnScreen(location)

                    val recognitionImg = Bitmap.createBitmap(
                        screenImg,
                        location[0],
                        location[1],
                        viewBinding.recognitionSquare.width,
                        viewBinding.recognitionSquare.height
                    )

                    (activity as MainActivity).setRecognitionImage(recognitionImg) // TODO! move it to storage

                    imageProxy.close()
                    viewBinding.root.findNavController().navigate(
                        R.id.action_cameraFragment_to_recognitionFragment)
                }
            }
        )
    }

    private fun takeFromGallery(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val imgUri: Uri = data.data!!

            val location = IntArray(2)
            viewBinding.recognitionSquare.getLocationOnScreen(location)

            var screenImg: Bitmap = MediaStore.Images.Media.getBitmap(
                requireActivity().contentResolver, imgUri)
            screenImg = scaleImageToDisplaySize(screenImg)

            (activity as MainActivity).setScreenImage(screenImg) // TODO! move it to storage

            val recognitionImg = Bitmap.createBitmap(screenImg, location[0], location[1],
                viewBinding.recognitionSquare.width,viewBinding.recognitionSquare.height
            )

            (activity as MainActivity).setRecognitionImage(recognitionImg)

            viewBinding.root.findNavController().navigate(
                R.id.action_cameraFragment_to_recognitionFragment)
        }
    }

    private fun toggleFlashLight() {
        isFlashLightOn = !isFlashLightOn
        camera.cameraControl.enableTorch(isFlashLightOn)
    }

    private fun arePermissionsGranted() = CAMERA_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
        if (degree == 0) {
            return img
        }

        val rotationMatrix = Matrix()
        rotationMatrix.postRotate(degree.toFloat())

        val rotatedImg =
            Bitmap.createBitmap(img, 0, 0, img.width, img.height, rotationMatrix, true)

        img.recycle()

        return rotatedImg
    }

    private fun scaleImageToDisplaySize(img: Bitmap): Bitmap {
        val displaySize = getDisplaySize()

        val scaledImg =
            Bitmap.createScaledBitmap(img, displaySize.width, displaySize.height, true)

        img.recycle()

        return scaledImg
    }

    private fun getDisplaySize(): Size {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        return Size(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

    private fun executeBack() {
        activity?.onBackPressedDispatcher?.onBackPressed()
    }
}
