package com.example.noteapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.noteapp.models.Category
import com.example.noteapp.models.Note

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes order by id desc")
    fun getAllNotes(): LiveData<List<Note>>

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

    @Query("select * from notes where categoryId = :categoryId OR categoryId IS NULL")
    fun getNotesByCategory(categoryId: Int?): LiveData<List<Note>>
}
