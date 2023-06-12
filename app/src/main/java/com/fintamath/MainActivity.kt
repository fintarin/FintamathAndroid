package com.fintamath

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fintamath.storage.HistoryStorage
import java.io.File
import java.util.*
import android.graphics.Bitmap


class MainActivity : AppCompatActivity() {
    // TODO! remove from MainActivity, move to new Storage class
    private lateinit var full_image: Bitmap
    private lateinit var cut_image: Bitmap

    // TODO! remove from MainActivity, move to new Storage class
    fun set_full_image(data: Bitmap) {
        full_image = data
    }
    fun get_full_image() : Bitmap {
        return full_image
    }
    fun set_cut_image(data: Bitmap) {
        cut_image = data
    }
    fun get_cut_image() : Bitmap {
        return cut_image
    }



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
}
