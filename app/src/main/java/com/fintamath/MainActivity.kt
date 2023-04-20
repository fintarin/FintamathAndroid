package com.fintamath

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fintamath.storage.HistoryStorage
import com.fintamath.storage.MathTextStorage
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
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

        val textFile = File(applicationContext.cacheDir.path + R.string.text_filename)
        textFile.createNewFile()
        MathTextStorage.loadFromFile(textFile)
    }

    private fun saveToFiles() {
        val historyFile = File(applicationContext.filesDir.path + R.string.history_filename)
        historyFile.createNewFile()
        HistoryStorage.saveToFile(historyFile)

        val textFile = File(applicationContext.cacheDir.path + R.string.text_filename)
        textFile.createNewFile()
        MathTextStorage.saveToFile(textFile)
    }
}
