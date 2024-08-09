package com.example.noteapp.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class NoteWithCategory(
    @Embedded val note: Note,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            NoteCategoryCrossRef::class,
            parentColumn = "noteId",
            entityColumn = "categoryId"
        )
    )
    val categories: List<Category>
)
