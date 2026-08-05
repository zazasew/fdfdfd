package com.cozynotes.app.data.local

import androidx.room.TypeConverter
import java.util.Date

/** Room type converters for the [Date] fields on [NoteEntity]. */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}
