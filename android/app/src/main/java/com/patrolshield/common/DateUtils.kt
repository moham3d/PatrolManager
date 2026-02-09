package com.patrolshield.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
