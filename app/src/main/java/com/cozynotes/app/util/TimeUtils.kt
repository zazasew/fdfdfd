package com.cozynotes.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeUtils {

    /** Returns a friendly greeting + emoji based on the current hour of day. */
    fun greeting(): Pair<String, String> {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning" to "☀️"
            in 12..16 -> "Good afternoon" to "🌤️"
            in 17..20 -> "Good evening" to "🌇"
            else -> "Good night" to "🌙"
        }
    }

    /** Short, human-friendly "last modified" label for a note card. */
    fun relativeLabel(date: Date): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply { time = date }

        val sameDay = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val wasYesterday = yesterday.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)

        return when {
            sameDay -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
            wasYesterday -> "Yesterday"
            now.get(Calendar.YEAR) == then.get(Calendar.YEAR) ->
                SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
            else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
        }
    }
}
