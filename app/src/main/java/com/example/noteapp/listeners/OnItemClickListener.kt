package com.example.noteapp.listeners

import com.example.noteapp.models.Note

interface OnItemClickListener {
    fun onNoteClick(note: Note, isChoose: Boolean)
    fun onNoteLongClick(note: Note)
}