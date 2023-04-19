package com.fintamath.fragment.history

import kotlinx.serialization.Serializable

@Serializable
data class HistoryItem(
    var text: String,
    var isBookmarked: Boolean,
    var dateTimeString: String,
)
