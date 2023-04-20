package com.fintamath.storage

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileReader
import java.io.FileWriter


object MathTextStorage {

    var text: String = ""

    fun loadFromFile(file: File) {
        var textData: String

        val reader = FileReader(file)
        reader.use {
            textData = reader.readText()
        }

        text = try {
            Json.decodeFromString(textData)
        } catch (exc: SerializationException) {
            ""
        }
    }

    fun saveToFile(file: File) {
        val historyData = try {
            Json.encodeToString(text)
        } catch (exc: SerializationException) {
            ""
        }

        val writer = FileWriter(file)
        writer.use {
            writer.write(historyData)
        }
    }
}
