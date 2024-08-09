package com.example.noteapp.repository

import com.example.noteapp.database.NoteDatabase
import com.example.noteapp.models.Category

class CategoryRepository(private val db: NoteDatabase) {

    suspend fun insertCategory(category: Category) = db.getCategoryDao().insertCategory(category)
    suspend fun deleteCategory(category: Category) = db.getCategoryDao().deleteCategory(category)
    suspend fun updateCategory(category: Category) = db.getCategoryDao().updateCategory(category)

    fun getAllCategory() = db.getCategoryDao().getAllCategories()
    fun getCategoryNameById(id: Int) = db.getCategoryDao().getCategoryNameById(id)
}