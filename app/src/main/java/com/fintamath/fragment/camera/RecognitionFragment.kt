package com.fintamath.fragment.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fintamath.MainActivity
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
import androidx.navigation.findNavController
import com.fintamath.R
import java.nio.ByteBuffer
import java.io.FileInputStream
import java.nio.channels.FileChannel
import java.nio.ByteOrder
import com.fintamath.calculator.CalculatorProcessor
import com.fintamath.storage.CalculatorInputStorage
import com.fintamath.storage.MathTextData
import com.fintamath.widget.fragment.BorderlessFragment

class RecognitionFragment : BorderlessFragment() {
    private lateinit var viewBinding: FragmentRecognitionBinding
    private var interpreter: Interpreter? = null

    private var inputImageWidth: Int = 0 // will be inferred from TF Lite model.
    private var inputImageHeight: Int = 0 // will be inferred from TF Lite model.
    private var modelInputSize: Int = 0 // will be inferred from TF Lite model.
    private lateinit var result: String

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
            val normalizedPixelValue = (r + g + b) / 3.0f / 255.0f
            byteBuffer.putFloat(normalizedPixelValue)
        }

        return byteBuffer
    }




    fun predictImage(bitmap: Bitmap): List<Mat> {
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
        var i = 0
        while (i<contours.size-1) {
            Log.d("NUMBER", i.toString())
            val grub_contours = contours
            var cnt1 = Imgproc.boundingRect(grub_contours[i])
            val x1 = cnt1.x
            val y1 = cnt1.y
            val w1 = cnt1.width
            val h1 = cnt1.height
            Log.d("NUMBERG", grub_contours.size.toString())
            for (j in 0..grub_contours.size-1){
                if (i==j){
                    continue
                }
                var cnt2 = Imgproc.boundingRect(grub_contours[j])
                val x2 = cnt2.x
                val y2 = cnt2.y
                val w2 = cnt2.width
                val h2 = cnt2.height

                if ((x1+w1>x2) and (x2>x1) and (y1+h1>y2) and (y1<y2)){
                    if (w1*h1>w2*h2){
                        contours.remove(grub_contours[j])
                    }
                    else{
                        contours.remove(grub_contours[i])
                    }
                    break
                }
                if ((x2+w2>x1) and (x1>x2) and (y2+h2>y1) and (y2<y1)){
                    if (w1*h1>w2*h2){
                        contours.remove(grub_contours[j])
                    }
                    else{
                        contours.remove(grub_contours[i])
                    }
                    break
                }
                if ((x1+w1>x2) and (x2>x1) and (y2+h2>y1) and (y2<y1)){
                    if (w1*h1>w2*h2){
                        contours.remove(grub_contours[j])
                    }
                    else{
                        contours.remove(grub_contours[i])
                    }
                    break
                }
                if ((x2+w2>x1) and (x1>x2) and (y1+h1>y2) and (y1<y2)){
                    if (w1*h1>w2*h2){
                        contours.remove(grub_contours[j])
                    }
                    else{
                        contours.remove(grub_contours[i])
                    }
                    break
                }

                if (j == grub_contours.size-1){
                    i+=1
                    break
                }
            }
        }

        val chars = mutableListOf<Mat>()

        for (c in contours) {
            val rect = Imgproc.boundingRect(c)
            val x = rect.x
            val y = rect.y
            val w = rect.width
            val h = rect.height
            Log.d("SIZE", w.toString()+" "+h.toString())
            //if (w >= 20 && w <= 150 && h >= 20 && h <= 120) {
            if (w >= 40 && h >= 0) {

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

                chars.add(padded)
            }
        }

        return chars
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentRecognitionBinding.inflate(layoutInflater, container, false)

        initializeInterpreter()
        OpenCVLoader.initDebug()

        viewBinding.recognitionBackButton.setOnClickListener { executeBack() }
        viewBinding.recognitionEditButton.setOnClickListener { showCalculatorFragment() }


        val screenImg: Bitmap = (activity as MainActivity).getScreenImage()
        val recognitionImg: Bitmap = (activity as MainActivity).getRecognitionImage()
        viewBinding.recognitionLayout.setBackgroundDrawable(BitmapDrawable(screenImg))

        val chars = predictImage(recognitionImg)
        result = ""
        for (char in chars) {
            val number = Bitmap.createBitmap(char.rows(), char.cols(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(char, number)


            val byteBuffer = convertBitmapToByteBuffer(number)
            val output = Array(1) { FloatArray(OUTPUT_CLASSES_COUNT) }
            interpreter?.run(byteBuffer, output)
            val prob = output[0]
            val maxIndex = prob.indices.maxByOrNull { prob[it] } ?: -1
            if (recognizable[maxIndex] == "pi") {
                result+="Pi"
            }
            else{
                if (recognizable[maxIndex] == "slash") {
                    result+="/"
                }
                else{
                    result += recognizable[maxIndex]
                }
            }

            if ((result[result.length-1] == '-') and (result.length>2)){
                if (result[result.length-2] == '-') {
                    result = result.substring(0..result.length - 3)
                    result += "="
                }
            }
        }

        CalculatorProcessor (
            { requireActivity().runOnUiThread { it.invoke() } },
            { outTexts(it) },
            { startLoading() }).calculate(result)


        viewBinding.recognitionInText.text = result


        return viewBinding.root
    }
    private fun startLoading() {
    }

    private fun outTexts(texts: List<String>) {
        viewBinding.recRez.text = texts[0]
    }

    private fun showCalculatorFragment() {
        CalculatorInputStorage.mathTextData = MathTextData(result)

        viewBinding.root.findNavController()
            .navigate(R.id.action_recognitionFragment_to_calculatorFragment)
    }

    private fun executeBack() {
        activity?.onBackPressedDispatcher?.onBackPressed()
    }

    companion object {
        private const val FLOAT_TYPE_SIZE = 4
        private const val PIXEL_SIZE = 1

        private const val OUTPUT_CLASSES_COUNT = 47
        private val recognizable = listOf<String>("(", ")", "+", "-", "<", "=", ">", "≤", "≥", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "pi", "q", "r", "s", "slash", "t", "u", "v", "w", "x", "y", "z")
    }
}
