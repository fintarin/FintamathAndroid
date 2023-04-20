package com.fintamath.storage

import kotlinx.serialization.Serializable

@Serializable
data class HistoryItemData(
    var mathTextData: MathTextData,
    var isBookmarked: Boolean,
    var dateTimeString: String,
)
