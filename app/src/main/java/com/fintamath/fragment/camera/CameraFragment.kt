package com.fintamath.camera

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.fintamath.MainActivity
import com.fintamath.R
import com.fintamath.databinding.FragmentCameraBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.widget.SeekBar
import androidx.camera.core.CameraControl


class CameraFragment : Fragment() {
    private lateinit var viewBinding: FragmentCameraBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraId: String
    private lateinit var camera: Camera
    private var isTurnFlashLight: Boolean = false
    private lateinit var zoomBar: SeekBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //OpenCVLoader.initDebug()
    }


 
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentCameraBinding.inflate(layoutInflater)

        viewBinding.cameraBackButton.setOnClickListener { viewBinding.root.findNavController().navigate(R.id.action_cameraFragment_to_calculatorFragment) }
        viewBinding.cameraHistoryButton.setOnClickListener { viewBinding.root.findNavController().navigate(R.id.action_cameraFragment_to_history) }
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }

        zoomBar = viewBinding.seekBar

        zoomBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                camera.cameraControl.setLinearZoom(progress / 100.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        hideSystemUI()


        viewBinding.flashLightButton.setOnClickListener { turnLight() }
        viewBinding.ImageFromGallery.setOnClickListener { takeFromGallery() }


        return viewBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        cameraExecutor.shutdown()
        restoreSystemUI()
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
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        Log.d(TAG, requestCode.toString())
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (areAllPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(context,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
        }
    }

    private fun startCamera()  {
        val context = requireContext()
        //val previewView = PreviewView(context)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setTargetRotation(viewBinding.viewFinder.display.rotation)
                .setTargetResolution(Size(720,1440))
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun takePhoto() {

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                @ExperimentalGetImage
                override fun onCaptureSuccess(imageProxy: ImageProxy){
                    val image = imageProxy.image
                    //val bitmap = Bitmap.createBitmap(image)
                    val buffer = image!!.planes[0].buffer
                    buffer.rewind()
                    val bytes = ByteArray(buffer.capacity())
                    buffer.get(bytes)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val location = IntArray(2)
                    viewBinding.focusExpr.getLocationOnScreen(location)
                    (activity as MainActivity).set_full_image(bitmap)
                    Log.d(TAG, image.width.toString()+" " +image.height.toString())
                    Log.d(TAG, bitmap.width.toString()+" " +bitmap.height.toString())
                    val cut = Bitmap.createBitmap(bitmap, location[0], location[1], viewBinding.focusExpr.getWidth(), viewBinding.focusExpr.getHeight())
                    (activity as MainActivity).set_cut_image(cut)
                    imageProxy.close()
                    viewBinding.root.findNavController().navigate(R.id.action_cameraFragment_to_recFragment)


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
            viewBinding.root.findNavController().navigate(R.id.action_cameraFragment_to_recFragment)
        }
    }

    private fun turnLight(){
        camera.cameraControl.enableTorch(! isTurnFlashLight)
        isTurnFlashLight = ! isTurnFlashLight
    }


    private fun areAllPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "Fintamath"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
            ).toTypedArray()
    }

    private fun executeBack() {
        activity?.onBackPressedDispatcher?.onBackPressed()
    }

    private fun hideSystemUI() {
        val window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }

    private fun restoreSystemUI() {
        val window = requireActivity().window
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
    }
}


