package com.fintamath

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fintamath.storage.HistoryStorage
import java.io.File
import android.graphics.Bitmap


class MainActivity : AppCompatActivity() {
    // TODO! remove from MainActivity, move to new Storage class
    private lateinit var screenImg: Bitmap
    private lateinit var recognitionImage: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadFromFiles()
    }

    override fun onPause() {
        super.onPause()

        saveToFiles()
    }

    private fun loadFromFiles() {
        val historyFile = File(applicationContext.filesDir.path + R.string.history_filename)
        historyFile.createNewFile()
        HistoryStorage.loadFromFile(historyFile)
    }

    private fun saveToFiles() {
        val historyFile = File(applicationContext.filesDir.path + R.string.history_filename)
        historyFile.createNewFile()
        HistoryStorage.saveToFile(historyFile)
    }

    // TODO! remove from MainActivity, move to new Storage class
    fun setScreenImage(data: Bitmap) {
        screenImg = data
    }

    fun getScreenImage() : Bitmap {
        return screenImg
    }

    fun setRecognitionImage(data: Bitmap) {
        recognitionImage = data
    }

    fun getRecognitionImage() : Bitmap {
        return recognitionImage
    }
}
