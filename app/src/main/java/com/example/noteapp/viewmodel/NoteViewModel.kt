package com.example.noteapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.noteapp.models.Note
import com.example.noteapp.repository.NoteRepository
import kotlinx.coroutines.launch

class NoteViewModel (app: Application, private val noteRepository: NoteRepository): AndroidViewModel(app){
    fun addNote(note: Note) = viewModelScope.launch {
        noteRepository.insertNote(note)
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        noteRepository.deleteNote(note)
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        noteRepository.updateNote(note)
    }

    fun getAllNote() = noteRepository.getAllNote()

    fun searchNote(query: String) = noteRepository.searchNote(query)

    fun deleteNotes(ids: List<Int>) = viewModelScope.launch {
        noteRepository.deleteByIds(ids)
    }

    fun getNotesWithoutCategory() = noteRepository.getNotesWithoutCategory()

    fun getNotesWithCategories() = noteRepository.getNotesWithCategories()

    fun getNotesByCategory(categoryId: Int) = noteRepository.getNotesByCategory(categoryId)

    fun getAllTrashNotes() = noteRepository.getAllTrashNotes()

    fun moveToTrash(ids: List<Int>) = viewModelScope.launch {
        noteRepository.moveToTrash(ids)
    }

    fun restoreFromTrash(ids: List<Int>) = viewModelScope.launch {
        noteRepository.restoreFromTrash(ids)
    }

    fun getLatestId() = noteRepository.getLatestId()
}