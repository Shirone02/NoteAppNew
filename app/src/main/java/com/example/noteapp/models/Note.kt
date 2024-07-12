package com.example.noteapp.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "content")
    val content: String,
    @ColumnInfo(name = "time")
    val time: String,
    @ColumnInfo(name = "created")
    val created: String,
    @ColumnInfo(name = "isInTrash")
    var isInTrash: Boolean = false,
)
