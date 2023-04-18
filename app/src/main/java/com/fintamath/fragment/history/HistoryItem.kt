package com.fintamath.fragment.history

import java.time.LocalDateTime

data class HistoryItem (
    var text: String,
    var isBookmarked: Boolean,
    var dateTime: LocalDateTime,
)
