package com.cozynotes.app.util

object TextStats {
    fun wordCount(text: String): Int =
        text.trim().let { if (it.isEmpty()) 0 else it.split(Regex("\\s+")).size }

    fun characterCount(text: String): Int = text.length
}
