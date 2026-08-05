package com.cozynotes.app.model

import com.cozynotes.app.data.local.NoteEntity
import java.util.Date
import java.util.UUID

/**
 * Domain-level note model used throughout the UI layer, kept separate from the
 * Room [NoteEntity] so the persistence layer can evolve without touching every screen.
 */
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val createdDate: Date = Date(),
    val modifiedDate: Date = Date(),
    val pinned: Boolean = false,
    val favorite: Boolean = false,
    val archived: Boolean = false,
    val color: Int? = null
)

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    content = content,
    createdDate = createdDate,
    modifiedDate = modifiedDate,
    pinned = pinned,
    favorite = favorite,
    archived = archived,
    color = color
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    content = content,
    createdDate = createdDate,
    modifiedDate = modifiedDate,
    pinned = pinned,
    favorite = favorite,
    archived = archived,
    color = color
)
