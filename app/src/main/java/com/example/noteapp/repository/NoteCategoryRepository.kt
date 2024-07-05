package com.example.noteapp.repository

import com.example.noteapp.database.NoteDatabase
import com.example.noteapp.models.NoteCategoryCrossRef

class NoteCategoryRepository(private val db: NoteDatabase) {
    suspend fun insert(noteCategoryCrossRef: NoteCategoryCrossRef) =
        db.noteCategoryCrossRefDao().insert(noteCategoryCrossRef)

    suspend fun insertNoteCategoryCrossRefs(noteCategoryCrossRefs: List<NoteCategoryCrossRef>) =
        db.noteCategoryCrossRefDao().insertNoteCategoryCrossRefs(noteCategoryCrossRefs)

    suspend fun deleteNoteCategoryCrossRefs(id: Int) =
        db.noteCategoryCrossRefDao().deleteNoteCategoryCrossRefs(id)
}