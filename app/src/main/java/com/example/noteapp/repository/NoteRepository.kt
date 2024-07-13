package com.example.noteapp.repository

import com.example.noteapp.database.NoteDatabase
import com.example.noteapp.models.Note

class NoteRepository(private val db: NoteDatabase) {
    suspend fun insertNote(note: Note) = db.getNoteDao().insertNote(note)
    suspend fun deleteNote(note: Note) = db.getNoteDao().deleteNote(note)
    suspend fun updateNote(note: Note) = db.getNoteDao().updateNote(note)
    suspend fun deleteByIds(ids: List<Int>) = db.getNoteDao().deleteByIds(ids)
    suspend fun moveToTrash(ids: List<Int>) = db.getNoteDao().moveToTrash(ids)
    suspend fun restoreFromTrash(ids: List<Int>) = db.getNoteDao().restoreFromTrash(ids)
    fun getLatestId() = db.getNoteDao().getLatestId()

    fun getNotesWithoutCategory() = db.getNoteDao().getNotesWithoutCategory()
    fun getAllTrashNotes() = db.getNoteDao().getAllTrashNotes()
    fun getAllNote() = db.getNoteDao().getAllNotes()
    fun searchNote(query: String) = db.getNoteDao().searchNote(query)
    fun getNotesWithCategories() = db.getNoteDao().getNotesWithCategories()
    fun getNotesByCategory(categoryId: Int) = db.getNoteDao().getNotesByCategory(categoryId)
}