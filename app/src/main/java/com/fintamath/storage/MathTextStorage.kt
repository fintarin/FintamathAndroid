package com.fintamath.storage

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileReader
import java.io.FileWriter


object MathTextStorage {

    var mathTextData: MathTextData = MathTextData("")

    fun loadFromFile(file: File) {
        var encodedMathTextData: String

        val reader = FileReader(file)
        reader.use {
            encodedMathTextData = reader.readText()
        }

        mathTextData = try {
            Json.decodeFromString(encodedMathTextData)
        } catch (exc: SerializationException) {
            MathTextData("")
        }
    }

    fun saveToFile(file: File) {
        val encodedMathTextData = try {
            Json.encodeToString(mathTextData)
        } catch (exc: SerializationException) {
            ""
        }

        val writer = FileWriter(file)
        writer.use {
            writer.write(encodedMathTextData)
        }
    }
}
