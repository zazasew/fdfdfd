package com.cozynotes.app.data.repository

import com.cozynotes.app.data.local.NoteDao
import com.cozynotes.app.model.Note
import com.cozynotes.app.model.toDomain
import com.cozynotes.app.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotesRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NotesRepository {

    override fun getAllNotes(): Flow<List<Note>> =
        noteDao.getAllNotes().map { list -> list.map { it.toDomain() } }

    override fun searchNotes(query: String): Flow<List<Note>> =
        noteDao.searchNotes(query).map { list -> list.map { it.toDomain() } }

    override fun observeNoteById(id: String): Flow<Note?> =
        noteDao.observeNoteById(id).map { it?.toDomain() }

    override suspend fun getNoteById(id: String): Note? =
        noteDao.getNoteById(id)?.toDomain()

    override suspend fun saveNote(note: Note) {
        noteDao.upsertNote(note.toEntity())
    }

    override suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note.toEntity())
    }

    override fun getNoteCount(): Flow<Int> = noteDao.getNoteCount()
}
