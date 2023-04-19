package com.fintamath.fragment.history

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.time.LocalDateTime
import kotlin.concurrent.thread


object HistoryStorage {

    var onItemsLoaded: ((Int, Int) -> Unit)? = null
    var onItemRemoved: ((Int) -> Unit)? = null
    var onItemInserted: ((Int) -> Unit)? = null

    private const val maxItemsNum = 50

    private var historyList = arrayListOf<HistoryItem>()

    fun loadFromFile(file: File) {
        var historyData = ""

        val reader = FileReader(file)
        reader.use {
            historyData = reader.readText()
        }

        historyList = try {
            Json.decodeFromString(historyData)
        } catch (exc: SerializationException) {
            arrayListOf()
        }

        onItemsLoaded?.invoke(0, historyList.size)
    }

    fun saveToFile(file: File) {
        val historyData = try {
            Json.encodeToString(historyList)
        } catch (exc: SerializationException) {
            ""
        }

        val writer = FileWriter(file)
        writer.use {
            writer.write(historyData)
        }
    }

    fun getList(): MutableList<HistoryItem> {
        return historyList
    }

    fun saveItem(text: String) {
        if (!isTextUnique(text)) {
            return
        }

        if (countNonBookmarkedItems() >= maxItemsNum) {
            removeLastNonBookmarkedItem()
            onItemRemoved?.invoke(maxItemsNum)
        }

        historyList.add(0, HistoryItem(text, false, LocalDateTime.now().toString()))
        onItemRemoved?.invoke(0)
    }

    fun removeItem(index: Int) {
        historyList.removeAt(index)
        onItemRemoved?.invoke(index)
    }

    fun setItemIsBookmarked(index: Int, isBookmarked: Boolean) {
        if (historyList[index].isBookmarked != isBookmarked) {
            historyList[index].isBookmarked = isBookmarked
        }
    }

    private fun removeLastNonBookmarkedItem() {
        val i = historyList.indexOfLast { !it.isBookmarked }
        historyList.removeAt(i)
        onItemRemoved?.invoke(i)
    }

    private fun countNonBookmarkedItems(): Int {
        return historyList.count { !it.isBookmarked }
    }

    private fun isTextUnique(text: String): Boolean {
        return !historyList.any { it.text == text }
    }
}
