package com.example.noteapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.models.Category
import com.example.noteapp.repository.CategoryRepository
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application, private val categoryRepository: CategoryRepository): AndroidViewModel(application) {
    fun addCategory(category: Category) = viewModelScope.launch {
        categoryRepository.insertCategory(category)
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        categoryRepository.deleteCategory(category)
    }

    fun updateCategory(category: Category) = viewModelScope.launch {
        categoryRepository.updateCategory(category)
    }

    fun getAllCategory() = categoryRepository.getAllCategory()

    fun getCategoryNameById(id: Int) = categoryRepository.getCategoryNameById(id)
}