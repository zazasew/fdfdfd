package com.cozynotes.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    /** Pinned notes first, then most recently modified first. */
    @Query(
        """
        SELECT * FROM notes
        WHERE archived = 0
        ORDER BY pinned DESC, modifiedDate DESC
        """
    )
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query(
        """
        SELECT * FROM notes
        WHERE archived = 0 AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY pinned DESC, modifiedDate DESC
        """
    )
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun observeNoteById(id: String): Flow<NoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    @Query("SELECT COUNT(*) FROM notes WHERE archived = 0")
    fun getNoteCount(): Flow<Int>
}
