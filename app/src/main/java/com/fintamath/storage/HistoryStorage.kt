package com.fintamath.storage

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.time.LocalDateTime


object HistoryStorage {

    var onItemsLoaded: ((Int, Int) -> Unit)? = null
    var onItemRemoved: ((Int) -> Unit)? = null
    var onItemMoved: ((Int, Int) -> Unit)? = null

    private const val maxItemsNum = 200

    private var historyList = arrayListOf<HistoryItemData>()

    fun loadFromFile(file: File) {
        var encodedHistoryList: String

        val reader = FileReader(file)
        reader.use {
            encodedHistoryList = reader.readText()
        }

        historyList = try {
            Json.decodeFromString(encodedHistoryList)
        } catch (exc: SerializationException) {
            arrayListOf()
        }

        onItemsLoaded?.invoke(0, historyList.size)
    }

    fun saveToFile(file: File) {
        val encodedHistoryList = try {
            Json.encodeToString(historyList)
        } catch (exc: SerializationException) {
            ""
        }

        val writer = FileWriter(file)
        writer.use {
            writer.write(encodedHistoryList)
        }
    }

    fun getItems(): List<HistoryItemData> {
        return historyList
    }

    fun saveItem(text: String) {
        val oldItemIndex = historyList.indexOfFirst { it.mathTextData.text == text }

        if (oldItemIndex != -1) {
            if (historyList[oldItemIndex].isBookmarked) {
                return
            }

            historyList.removeAt(oldItemIndex)
            onItemRemoved?.invoke(oldItemIndex)
        }

        if (countNonBookmarkedItems() >= maxItemsNum) {
            removeLastNonBookmarkedItem()
            onItemRemoved?.invoke(maxItemsNum)
        }

        historyList.add(countBookmarkedItems(), HistoryItemData(
            MathTextData(text),
            false,
            LocalDateTime.now().toString()
        ))
    }

    fun removeItem(index: Int) {
        historyList.removeAt(index)
        onItemRemoved?.invoke(index)
    }

    fun bookmarkItem(index: Int, isBookmarked: Boolean) {
        if (historyList[index].isBookmarked == isBookmarked) {
            return
        }

        historyList[index].isBookmarked = isBookmarked
        moveItemOnBookmarkChange(index)
    }

    private fun moveItemOnBookmarkChange(index: Int) {
        val historyItem = historyList[index]
        historyList.removeAt(index)

        val newIndex = findNewIndexOfItemOnBookmarkChange(historyItem)
        historyList.add(newIndex, historyItem)

        onItemMoved?.invoke(index, newIndex)
    }

    private fun findNewIndexOfItemOnBookmarkChange(item: HistoryItemData): Int {
        val dateTime = LocalDateTime.parse(item.dateTimeString)

        val index = historyList.indexOfFirst {
            if (it.isBookmarked != item.isBookmarked) {
                return@indexOfFirst false
            }

            val itDateTime = LocalDateTime.parse(it.dateTimeString)
            return@indexOfFirst itDateTime.isBefore(dateTime)
        }

        if (index == -1) {
            return if (item.isBookmarked) countBookmarkedItems() else historyList.size
        }

        return index
    }

    private fun removeLastNonBookmarkedItem() {
        if (historyList.isEmpty() || historyList.last().isBookmarked) {
            return
        }

        historyList.removeAt(historyList.size - 1)
        onItemRemoved?.invoke(historyList.size - 1)
    }

    private fun countBookmarkedItems(): Int {
        return historyList.count { it.isBookmarked }
    }

    private fun countNonBookmarkedItems(): Int {
        return historyList.count { !it.isBookmarked }
    }
}
