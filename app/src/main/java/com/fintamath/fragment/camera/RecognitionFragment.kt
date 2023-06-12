package com.fintamath.fragment.camera

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.fintamath.MainActivity
import com.fintamath.R
import com.fintamath.databinding.FragmentRecognitionBinding
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.android.OpenCVLoader
import org.opencv.imgproc.Imgproc
import android.util.Log
import org.tensorflow.lite.Interpreter
import android.content.res.AssetManager
import java.nio.ByteBuffer
import java.io.FileInputStream
import java.nio.channels.FileChannel
import java.nio.ByteOrder

class RecognitionFragment : Fragment() {
    private lateinit var viewBinding: FragmentRecognitionBinding
    private var interpreter: Interpreter? = null

    private var inputImageWidth: Int = 0 // will be inferred from TF Lite model.
    private var inputImageHeight: Int = 0 // will be inferred from TF Lite model.
    private var modelInputSize: Int = 0 // will be inferred from TF Lite model.

    private fun initializeInterpreter() {
        val assetManager = requireContext().assets
        val model = loadModelFile(assetManager, "recognition_model/model.tflite")
        val interpreter = Interpreter(model)
        val inputShape = interpreter.getInputTensor(0).shape()
        inputImageWidth = inputShape[1]
        inputImageHeight = inputShape[2]
        modelInputSize = FLOAT_TYPE_SIZE * inputImageWidth *
                inputImageHeight * PIXEL_SIZE
        // Finish interpreter initialization.
        this.interpreter = interpreter

    }

    private fun loadModelFile(assetManager: AssetManager, filename: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputImageWidth * inputImageHeight)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in pixels) {
            val r = (pixelValue shr 16 and 0xFF)
            val g = (pixelValue shr 8 and 0xFF)
            val b = (pixelValue and 0xFF)
            //Log.d("PIXEL", r.toString()+ " " + g.toString()+ " " + b.toString())
            val normalizedPixelValue = (r + g + b) / 3.0f / 255.0f
            byteBuffer.putFloat(normalizedPixelValue)
        }

        return byteBuffer
    }




    fun predictImage(bitmap: Bitmap): List<Pair<Mat, Rect>> {
        val image = Mat()
        Utils.bitmapToMat(bitmap, image)
        val gray = Mat()
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY)
        val blurred = Mat()
        Imgproc.GaussianBlur(image, blurred, Size(5.0, 5.0), 0.0)
        val edged = Mat()
        Imgproc.Canny(blurred, edged, 30.0, 150.0)

        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(edged, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
        contours.sortBy { Imgproc.boundingRect(it).x }
        val chars = mutableListOf<Pair<Mat, Rect>>()

        for (c in contours) {
            val rect = Imgproc.boundingRect(c)
            val x = rect.x
            val y = rect.y
            val w = rect.width
            val h = rect.height
            Log.d("SIZE", w.toString()+" "+h.toString())
            //if (w >= 20 && w <= 150 && h >= 20 && h <= 120) {
            if (w >= 30 && h >= 30) {

                val roi = gray.submat(rect)
                val thresh = Mat()
                Imgproc.threshold(roi, thresh, 0.0, 255.0, Imgproc.THRESH_BINARY_INV or Imgproc.THRESH_OTSU)
                val tH = thresh.height()
                val tW = thresh.width()
                val resized = if (tW > tH) {
                    val resizedWidth = 45
                    val resizedHeight = (tH * (resizedWidth.toDouble() / tW.toDouble())).toInt()
                    val resized = Mat()
                    Imgproc.resize(thresh, resized, Size(resizedWidth.toDouble(), resizedHeight.toDouble()))
                    resized
                } else {
                    val resizedHeight = 45
                    val resizedWidth = (tW * (resizedHeight.toDouble() / tH.toDouble())).toInt()
                    val resized = Mat()
                    Imgproc.resize(thresh, resized, Size(resizedWidth.toDouble(), resizedHeight.toDouble()))
                    resized
                }
                val padded = Mat()
                val dX = maxOf(0, 45 - tW) / 2
                val dY = maxOf(0, 45 - tH) / 2

                Core.copyMakeBorder(resized, padded, dY , dY, dX, dX, Core.BORDER_CONSTANT, Scalar(0.0, 0.0, 0.0))
                Imgproc.resize(padded, padded, Size(45.0, 45.0))
                //padded.convertTo(padded, CvType.CV_32F)

                chars.add(Pair(padded, rect))
            }
        }

        return chars
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        OpenCVLoader.initDebug()

        viewBinding = FragmentRecognitionBinding.inflate(layoutInflater)
        viewBinding.recBackButton.setOnClickListener { viewBinding.root.findNavController().navigate(R.id.action_recFragment_to_cameraFragment) }

        val full_image: Bitmap = (activity as MainActivity).get_full_image()
        val cut_image: Bitmap = (activity as MainActivity).get_cut_image()
        viewBinding.recLayout.setBackgroundDrawable(BitmapDrawable(full_image))
        viewBinding.cutImage.setBackgroundDrawable(BitmapDrawable(cut_image))

        val numbers = predictImage(cut_image)
        Log.d("FRAGMENTS", numbers.size.toString())
        val recognized = numbers[0].first
        val number = Bitmap.createBitmap(recognized.rows(), recognized.cols(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(recognized, number)
        //val imageProcessor = ImageProcessor.Builder().build()
        //val tensorImage = imageProcessor.process(TensorImage.fromBitmap(number))

        initializeInterpreter()
        val byteBuffer = convertBitmapToByteBuffer(number)
        val output = Array(1) { FloatArray(OUTPUT_CLASSES_COUNT) }
        interpreter?.run(byteBuffer, output)
        val result = output[0]
        val maxIndex = result.indices.maxByOrNull { result[it] } ?: -1
        val resultString =
            "Prediction Result: %d\nConfidence: %2f"
                .format(maxIndex, result[maxIndex])
        Log.d("RESULT",resultString )

//        TfLiteVision.initialize(context)
//        val optionsBuilder = ImageClassifier.ImageClassifierOptions.builder()
//        val baseOptionsBuilder = BaseOptions.builder()
//        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())
//        val imageClassifier =
//            ImageClassifier.createFromFileAndOptions(context, modelName, optionsBuilder.build())







        //viewBinding.cutImage.setBackgroundDrawable(BitmapDrawable(number))
        //Log.d("SIZE", results.toString())



        return viewBinding.root

    }

    companion object {
        private const val TAG = "DigitClassifier"

        private const val FLOAT_TYPE_SIZE = 4
        private const val PIXEL_SIZE = 1

        private const val OUTPUT_CLASSES_COUNT = 47
    }
}
