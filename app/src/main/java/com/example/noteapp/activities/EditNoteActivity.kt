package com.example.noteapp.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.noteapp.database.NoteDatabase
import com.example.noteapp.databinding.ActivityEditNoteBinding
import com.example.noteapp.models.Note
import com.example.noteapp.repository.NoteRepository
import com.example.noteapp.viewmodel.NoteViewModel
import com.example.noteapp.viewmodel.NoteViewModelFactory
import java.util.Calendar

class EditNoteActivity : AppCompatActivity() {

    private val binding: ActivityEditNoteBinding by lazy {
        ActivityEditNoteBinding.inflate(layoutInflater)
    }

    private lateinit var noteViewModel: NoteViewModel
    private lateinit var currentContent: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setUpViewModel()

        binding.backBtn.setOnClickListener {
            saveNote()
        }

        binding.saveBtn.setOnClickListener { saveNote() }

        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")

        currentContent = content.toString()

        binding.edtTitle.setText(title)
        binding.edtContent.setText(content)
        binding.undoBtn.setOnClickListener { undoNote() }
    }

    private fun saveNote() {
        val id = intent.getIntExtra("id", 0)
        val categoryId = intent.getIntExtra("categoryId", 0)

        val noteTitle = binding.edtTitle.text.toString()
        val noteContent = binding.edtContent.text.toString()

        if(categoryId == 0) {
            val note = Note(id, noteTitle, noteContent, getCurrentTime(), null)
            noteViewModel.updateNote(note)
        } else {
            val note = Note(id, noteTitle, noteContent, getCurrentTime(), categoryId)
            noteViewModel.updateNote(note)
        }

        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun undoNote() {
        binding.edtContent.setText(currentContent)
    }

    private fun setUpViewModel() {
        val noteRepository = NoteRepository(NoteDatabase(this))

        val viewModelProviderFactory = NoteViewModelFactory(application, noteRepository)

        noteViewModel = ViewModelProvider(this, viewModelProviderFactory)[NoteViewModel::class.java]
    }

    private fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Tháng trong Calendar bắt đầu từ 0
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        val formattedDate = "$day/$month/$year $hour:$minute:$second"
        return formattedDate
    }
}