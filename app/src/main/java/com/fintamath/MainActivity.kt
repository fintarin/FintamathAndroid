package com.fintamath

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fintamath.storage.HistoryStorage
import com.fintamath.storage.SettingsStorage
import java.io.File


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SettingsStorage.init(getPreferences(Context.MODE_PRIVATE), resources)
        loadFromFiles()
    }

    override fun onPause() {
        super.onPause()

        saveToFiles()
    }

    private fun loadFromFiles() {
        val historyFile = File(applicationContext.filesDir.path + "/" + getString(R.string.history_filename))
        historyFile.createNewFile()
        HistoryStorage.loadFromFile(historyFile)
    }

    private fun saveToFiles() {
        val historyFile = File(applicationContext.filesDir.path + "/" + getString(R.string.history_filename))
        historyFile.createNewFile()
        HistoryStorage.saveToFile(historyFile)
    }
}
