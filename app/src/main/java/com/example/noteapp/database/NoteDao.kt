package com.example.noteapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.noteapp.models.Note
import com.example.noteapp.models.NoteWithCategory

@Dao
interface NoteDao {
    @Query("select * from notes")
    fun getNotesWithCategories(): LiveData<List<NoteWithCategory>>

    @Query(
        """
        SELECT notes.* 
        FROM notes 
        LEFT JOIN note_category_cross_ref 
        ON notes.id = note_category_cross_ref.noteId 
        WHERE note_category_cross_ref.categoryId IS NULL and isInTrash = 0
    """
    )
    fun getNotesWithoutCategory(): LiveData<List<Note>>

    @Query(
        """
        SELECT notes.* 
        FROM notes 
        LEFT JOIN note_category_cross_ref 
        ON notes.id = note_category_cross_ref.noteId 
        WHERE note_category_cross_ref.categoryId = :categoryId
    """
    )
    fun getNotesByCategory(categoryId: Int): LiveData<List<Note>>

    @Query("select * from notes where isInTrash = 0 order by id desc ")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("select * from notes where isInTrash = 1 order by id desc ")
    fun getAllTrashNotes(): LiveData<List<Note>>

    @Query("update notes set isInTrash = 1 where id in (:ids)")
    suspend fun moveToTrash(ids: List<Int>)

    @Query("update notes set isInTrash = 0 where id in (:ids)")
    suspend fun restoreFromTrash(ids: List<Int>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Query("SELECT * FROM notes WHERE title LIKE :query OR content LIKE :query ")
    fun searchNote(query: String): LiveData<List<Note>>

    @Query("DELETE FROM notes WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    @Query("SELECT MAX(id) FROM notes where isInTrash = 0")
    fun getLatestId(): Int

    @Query("Select color from notes where id = :id")
    fun getColor(id: Int): String

    @Query("select * from notes where id = :id")
    fun getNoteById(id: Int): Note
}
