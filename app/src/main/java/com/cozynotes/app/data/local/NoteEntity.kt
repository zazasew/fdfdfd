package com.cozynotes.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * Room entity representing a single note stored on-device.
 * Nothing here ever leaves the device — there is no network layer in this app.
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val createdDate: Date = Date(),
    val modifiedDate: Date = Date(),
    val pinned: Boolean = false,
    val favorite: Boolean = false,
    val archived: Boolean = false,
    /** ARGB color int, or null for the default surface color. */
    val color: Int? = null
)
