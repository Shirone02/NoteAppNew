package com.example.noteapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.models.NoteCategoryCrossRef
import com.example.noteapp.repository.NoteCategoryRepository
import kotlinx.coroutines.launch

class NoteCategoryViewModel(
    application: Application,
    private val noteCategoryRepository: NoteCategoryRepository
) : AndroidViewModel(application) {
    fun addNoteCategory(noteCategory: NoteCategoryCrossRef) = viewModelScope.launch {
        noteCategoryRepository.insert(noteCategory)
    }

    fun addListNoteCategory(noteCategoryCrossRefs: List<NoteCategoryCrossRef>) =
        viewModelScope.launch {
            noteCategoryRepository.insertNoteCategoryCrossRefs(noteCategoryCrossRefs)
        }

    fun deleteNoteCategoryCrossRefs(id: Int) =
        viewModelScope.launch {
            noteCategoryRepository.deleteNoteCategoryCrossRefs(id)
        }
}