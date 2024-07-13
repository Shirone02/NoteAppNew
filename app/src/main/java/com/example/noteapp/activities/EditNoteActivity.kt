package com.example.noteapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.noteapp.R
import com.example.noteapp.adapter.ListCategoryAdapter
import com.example.noteapp.database.NoteDatabase
import com.example.noteapp.databinding.ActivityEditNoteBinding
import com.example.noteapp.models.Category
import com.example.noteapp.models.Note
import com.example.noteapp.models.NoteCategoryCrossRef
import com.example.noteapp.repository.CategoryRepository
import com.example.noteapp.repository.NoteCategoryRepository
import com.example.noteapp.repository.NoteRepository
import com.example.noteapp.viewmodel.CategoryViewModel
import com.example.noteapp.viewmodel.CategoryViewModelFactory
import com.example.noteapp.viewmodel.NoteCategoryViewModel
import com.example.noteapp.viewmodel.NoteCategoryViewModelFactory
import com.example.noteapp.viewmodel.NoteViewModel
import com.example.noteapp.viewmodel.NoteViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.log

class EditNoteActivity : AppCompatActivity() {

    private val binding: ActivityEditNoteBinding by lazy {
        ActivityEditNoteBinding.inflate(layoutInflater)
    }

    private lateinit var noteViewModel: NoteViewModel
    private lateinit var currentContent: String
    private val textUndo = mutableListOf<Pair<String, Int>>()
    private val textRedo = mutableListOf<Pair<String, Int>>()
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var noteCategoryViewModel: NoteCategoryViewModel
    private lateinit var categoryAdapter: ListCategoryAdapter
    private lateinit var categories: List<Category>
    private var isUndo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setUpViewModel()

        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")

        currentContent = content.toString()

        binding.edtTitle.setText(title)
        binding.edtContent.setText(content)

        binding.topAppBar.setNavigationOnClickListener {
            saveNote()
            finish()
        }

        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.Save -> {
                    saveNote()
                    true
                }

                R.id.Undo -> {
                    undoNote()
                    true
                }

                R.id.Redo -> {
                    redoNote()
                    true
                }

                R.id.undo_all -> {
                    undoAll()
                    true
                }

                R.id.Share -> {
                    shareNote()
                    true
                }

                R.id.export_text_a_file -> {
                    exportNoteToTextFile()
                    true
                }

                R.id.delete -> {
                    deleteNote()
                    true
                }

                R.id.search -> {
                    false
                }

                R.id.categorize_note -> {
                    showCategorizeDialog()
                    true
                }

                R.id.Colorize -> {
                    false
                }

                R.id.switch_to_read_mode -> {
                    false
                }

                R.id.print -> {
                    false
                }

                R.id.show_formatting_bar -> {
                    false
                }

                R.id.showInfo -> {
                    showInfoDialog()
                    true
                }

                else -> {
                    false
                }
            }
        }

        binding.edtContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!isUndo) {
                    textUndo.add(
                        Pair(
                            binding.edtContent.text.toString(),
                            binding.edtContent.selectionStart
                        )
                    )
                } else {
                    isUndo = false
                }
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }

    //chia se note
    private fun shareNote() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, null))
    }

    //hien thi ra info
    private fun showInfoDialog() {
        val words =
            binding.edtContent.text.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        val characters = binding.edtContent.text.count()
        val charactersWithoutWhitespaces =
            binding.edtContent.text.filter { !it.isWhitespace() }.length
        val created = intent.getStringExtra("created")
        val time = intent.getStringExtra("time")

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            .setMessage("Words: $words \nWrapped lines: 1 \nCharacters: $characters \nCharacters without whitespaces: $charactersWithoutWhitespaces \nCreated at: $created \nLast saved at: $time")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }

    //xoa note
    private fun deleteNote() {
        val message = if (binding.edtTitle.text.toString() == "") {
            "Untitled"
        } else {
            binding.edtTitle.text
        }
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

            .setMessage("The '$message' note will be deleted. Are you sure?")
            .setPositiveButton("Delete") { dialog, which ->
                val id = intent.getIntExtra("id", 0)
                val title = intent.getStringExtra("title")
                val content = intent.getStringExtra("content")
                val categoryId = intent.getIntExtra("categoryId", 0)
                val created = intent.getStringExtra("created")
                val time = intent.getStringExtra("time")

                val note =
                    Note(
                        id,
                        title.toString(),
                        content.toString(),
                        time.toString(),
                        created.toString(),
                        false
                    )
                noteViewModel.deleteNote(note)
                finish()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    //them note vao cac category
    private fun showCategorizeDialog() {
        val checkedItem = BooleanArray(categories.size)

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Select category")
            .setPositiveButton("OK") { dialog, which ->
                val selectedCategories = mutableListOf<Category>()
                val unSelectedCategories = mutableListOf<Category>()
                for (i in categories.indices) {
                    if (checkedItem[i]) {
                        selectedCategories.add(categories[i])
                    } else {
                        unSelectedCategories.add(categories[i])
                    }
                }

                // Tạo danh sách NoteCategoryCrossRef để liên kết note với category
                val id = intent.getIntExtra("id", 0)

                for (categoryId in selectedCategories.map { it.id }) {
                    noteCategoryViewModel.addNoteCategory(NoteCategoryCrossRef(id, categoryId))
                }

                Toast.makeText(
                    this,
                    "Updated categories",
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()
            }

            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }

            .setMultiChoiceItems(
                categories.map { it.categoryName }.toTypedArray(),
                checkedItem
            ) { dialog, which, isChecked ->
                checkedItem[which] = isChecked
            }
        builder.create().show()
    }

    //Export note ra file txt
    private fun exportNoteToTextFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "${binding.edtTitle.text}.txt")
        }
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                this.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val content = binding.edtContent.text.toString()
                    outputStream.write(content.toByteArray())
                }
            }
            Toast.makeText(this, "1 note(s) exported", Toast.LENGTH_SHORT)
                .show()
        }

    }

    companion object {
        private const val CREATE_FILE_REQUEST_CODE = 1
    }

    private fun saveNote() {
        val id = intent.getIntExtra("id", 0)
        val created = intent.getStringExtra("created")

        val noteTitle = binding.edtTitle.text.toString()
        val noteContent = binding.edtContent.text.toString()

        if(id == 0){
            val note = Note(noteViewModel.getLatestId(), noteTitle, noteContent, getCurrentTime(), created!!, false)
            noteViewModel.updateNote(note)
        } else {
            val note = Note(id, noteTitle, noteContent, getCurrentTime(), created!!, false)
            noteViewModel.updateNote(note)
        }
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
    }

    //redo
    private fun redoNote() {
        if (textRedo.isNotEmpty()) {
            val (previousText, previousCursorPosition) = textRedo.removeLast()
            textUndo.add(Pair(previousText, previousCursorPosition))
            binding.edtContent.setText(previousText)
            binding.edtContent.setSelection(previousCursorPosition)
        }
    }

    //undo
    private fun undoNote() {
        if (textUndo.isNotEmpty()) {
            isUndo = true
            val (previousText, previousCursorPosition) = textUndo.removeLast()
            textRedo.add(Pair(previousText, previousCursorPosition))
            binding.edtContent.setText(previousText)
            binding.edtContent.setSelection(previousCursorPosition)
        }
    }

    //undo tat ca
    private fun undoAll() {
        binding.edtContent.setText(currentContent)
    }

    private fun setUpViewModel() {
        val noteRepository = NoteRepository(NoteDatabase(this))
        val viewModelProviderFactory = NoteViewModelFactory(application, noteRepository)
        noteViewModel = ViewModelProvider(this, viewModelProviderFactory)[NoteViewModel::class.java]

        val categoryRepository = CategoryRepository(NoteDatabase(this))
        val cateViewModelProviderFactory = CategoryViewModelFactory(application, categoryRepository)
        categoryViewModel =
            ViewModelProvider(this, cateViewModelProviderFactory)[CategoryViewModel::class.java]

        val noteCategoryRepository = NoteCategoryRepository(NoteDatabase(this))
        val noteCategoryViewModelFactory =
            NoteCategoryViewModelFactory(application, noteCategoryRepository)
        noteCategoryViewModel =
            ViewModelProvider(this, noteCategoryViewModelFactory)[NoteCategoryViewModel::class.java]

        categoryAdapter = ListCategoryAdapter(this)

        this.let {
            categoryViewModel.getAllCategory().observe(this) { category ->
                categoryAdapter.differ.submitList(category)
                categories = categoryAdapter.differ.currentList
            }
        }
    }

    private fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()

        val formattedDate =
            SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault()).format(calendar.time)

        return formattedDate
    }
}