package com.cozynotes.app.data.repository

import com.cozynotes.app.model.Note
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun getAllNotes(): Flow<List<Note>>
    fun searchNotes(query: String): Flow<List<Note>>
    fun observeNoteById(id: String): Flow<Note?>
    suspend fun getNoteById(id: String): Note?
    suspend fun saveNote(note: Note)
    suspend fun deleteNote(note: Note)
    fun getNoteCount(): Flow<Int>
}
