package com.example.noteapp.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint.Style
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.Image
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.SpannedString
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.util.Size
import android.view.MenuItem
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.adapter.ListCategoryAdapter
import com.example.noteapp.adapter.ListColorAdapter
import com.example.noteapp.adapter.ListNoteAdapter
import com.example.noteapp.database.NoteDatabase
import com.example.noteapp.databinding.ActivityEditNoteBinding
import com.example.noteapp.listeners.OnColorClickListener
import com.example.noteapp.listeners.OnItemClickListener
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
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.log

class EditNoteActivity : AppCompatActivity(), OnColorClickListener {

    private val binding: ActivityEditNoteBinding by lazy {
        ActivityEditNoteBinding.inflate(layoutInflater)
    }

    private lateinit var noteViewModel: NoteViewModel
    private lateinit var currentContent: String
    private val textUndo = mutableListOf<Pair<String, Int>>()
    private val textRedo = mutableListOf<Pair<String, Int>>()
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var noteCategoryViewModel: NoteCategoryViewModel

    private lateinit var noteAdapter: ListNoteAdapter
    private lateinit var categoryAdapter: ListCategoryAdapter
    private lateinit var colorAdapter: ListColorAdapter
    private lateinit var categories: List<Category>

    private var isUndo = false
    private var selectedColor: String? = null
    private var selectedFormatTextColor: String? = null
    private var selectedTextSize: Int = 18

    private val colors = listOf(
        "#FFCDD2", "#F8BBD0", "#E1BEE7", "#D1C4E9", "#C5CAE9",
        "#BBDEFB", "#B3E5FC", "#B2EBF2", "#B2DFDB", "#C8E6C9",
        "#DCEDC8", "#F0F4C3", "#FFECB3", "#FFE0B2", "#FFCCBC",
        "#D7CCC8", "#F5F5F5", "#CFD8DC", "#FF8A80", "#FF80AB"
    )

    private val formatColor = listOf(
        "#000000", "#FF0000", "#00FF00", "#0000FF",
        "#FFFF00", "#FF00FF", "#00FFFF", "#FFFFFF",
        "#800000", "#008000", "#000080", "#808000",
        "#800080", "#008080", "#808080", "#C0C0C0",
        "#400000", "#004000", "#000040", "#404000",
        "#400040", "#004040", "#404040", "#600000",
        "#008000", "#008000", "#800080", "#008080",
        "#800000", "#008000", "#800080", "#008080",
        "#200000", "#002000", "#000020", "#202000",
        "#200020", "#002020", "#202020", "#600000",
        "#008000", "#008000", "#800080", "#008080",
        "#800000", "#008000", "#800080", "#008080"
    )

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setUpViewModel()

        loadNote()

        formattingBarAction()

        binding.topAppBar.setNavigationOnClickListener {
            saveNote()
            finish()
        }

        val save = SpannableString(binding.topAppBar.menu.findItem(R.id.Save).title)
        save.setSpan(ForegroundColorSpan(Color.WHITE), 0, save.length, 0)
        binding.topAppBar.menu.findItem(R.id.Save).title = save

        val undo = SpannableString(binding.topAppBar.menu.findItem(R.id.Undo).title)
        undo.setSpan(ForegroundColorSpan(Color.WHITE), 0, undo.length, 0)
        binding.topAppBar.menu.findItem(R.id.Undo).title = undo

        binding.topAppBar.overflowIcon?.setTint(Color.WHITE)

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

                R.id.search_note -> {
                    true
                }

                R.id.categorize_note -> {
                    showCategorizeDialog()
                    true
                }

                R.id.Colorize -> {
                    showColorPickerDialog()
                    true
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

    //hanh dong thanh formattingBar
    private fun formattingBarAction() {
//        binding.formattingToolbar.setOnMenuItemClickListener {
//            when (it.itemId) {
//                R.id.bold -> {
//                    applyStyle(Typeface.BOLD)
//                    true
//                }
//
//                R.id.italic -> {
//                    applyStyle(Typeface.ITALIC)
//                    true
//                }
//
//                R.id.underline -> {
//                    applyUnderline()
//                    true
//                }
//
//                R.id.strike_through -> {
//                    strikeThrough(binding.edtContent)
//                    true
//                }
//
//                R.id.fill_color -> {
//                    showFormatColorPickerDialog(R.id.fill_color)
//                    true
//                }
//
//                R.id.text_color -> {
//                    showFormatColorPickerDialog(R.id.text_color)
//                    true
//                }
//
//                R.id.text_size -> {
//                    false
//                }
//
//                else -> {
//                    false
//                }
//            }
//        }

        binding.bold.setOnClickListener {
            applyStyle(Typeface.BOLD)
        }

        binding.italic.setOnClickListener {
            applyStyle(Typeface.ITALIC)
        }

        binding.underline.setOnClickListener {
            applyUnderline()
        }

        binding.strikeThrough.setOnClickListener {
            strikeThrough(binding.edtContent)
        }

        binding.fillColor.setOnClickListener {
            showFormatColorPickerDialog(binding.fillColor)
        }

        binding.textColor.setOnClickListener {
            showFormatColorPickerDialog(binding.textColor)
        }

        binding.textSize.setOnClickListener {
            showFormatTextSizeDialog(binding.edtContent)
        }
    }

    //tai note hien tai
    @SuppressLint("ResourceAsColor")
    private fun loadNote() {
        val id = intent.getIntExtra("id", 0)
        val title = intent.getStringExtra("title")
        val color = intent.getStringExtra("color")
        val content: String = if (id == 0) {
            intent.getStringExtra("content").toString()
        } else {
            noteViewModel.getNoteById(id).content
        }

        if(color != null){
            val backgroundDrawable = GradientDrawable()
            backgroundDrawable.setColor(Color.parseColor(color))
            backgroundDrawable.setStroke(4, R.color.brown)
            binding.editNote.background = backgroundDrawable
            binding.main.background = backgroundDrawable
        }

        currentContent = content

        binding.edtTitle.setText(title)
        binding.edtContent.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY))
    }

    //gach ngang giua van ban
    private fun strikeThrough(editText: EditText) {
        val start = binding.edtContent.selectionStart
        val end = binding.edtContent.selectionEnd

        if (start < end) {
            val spannable = editText.text as Spannable
            val spans = spannable.getSpans(start, end, StrikethroughSpan::class.java)

            if (spans.isNotEmpty()) {
                //xoa bo gach ngang giua neu da co
                for (span in spans) {
                    spannable.removeSpan(span)
                }
            } else {
                //them gach ngang giua neu chua co
                spannable.setSpan(
                    StrikethroughSpan(),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    //ap dung style dam, nghieng cho text
    private fun applyStyle(style: Int) {
        Log.d("span", "applyStyle: span list")
        val start = binding.edtContent.selectionStart
        val end = binding.edtContent.selectionEnd
        Log.d("span", "start: $start - $end")

        if (start < end) {
            val spannable = binding.edtContent.text as Spannable
            val spans = spannable.getSpans(start, end,StyleSpan::class.java)

            val isAllFormatted = spans.size == 1 && spannable.getSpanStart(spans[0]) <= start && spannable.getSpanEnd(spans[0]) >= end

            spans.forEach { span ->
                Log.d("span", "spans: $span")
            }
                if(spans.isNotEmpty()){
                    Log.d("span", "applyStyle: $isAllFormatted")
                    Log.d("span", "size: ${spans.size}")
                    Log.d("span", "start: ${spannable.getSpanStart(spans[0])}")
                    Log.d("span", "end: ${spannable.getSpanEnd(spans[0])}")
                }

            if(isAllFormatted){
                spannable.removeSpan(spans[0])
            } else {
                spannable.setSpan(StyleSpan(style), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    //ap dung style gach chan
    private fun applyUnderline() {
        val start = binding.edtContent.selectionStart
        val end = binding.edtContent.selectionEnd

        if (start < end) {
            val spannable = SpannableStringBuilder(binding.edtContent.text)
            spannable.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.edtContent.text = spannable
            binding.edtContent.setSelection(start, end)
        }
    }

    //chinh sua co chu cho edit text
    @SuppressLint("SetTextI18n")
    private fun showFormatTextSizeDialog(editText: EditText) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_format_text_size, null)
        val defaultSize = dialogView.findViewById<Button>(R.id.defaultSizeBtn)
        val textSize = dialogView.findViewById<SeekBar>(R.id.sbTextSize)
        val textPreview = dialogView.findViewById<TextView>(R.id.textPreview)
        textPreview.text = "Text size $selectedTextSize"

        defaultSize.setOnClickListener {
            selectedTextSize = 18
            textPreview.text = "Text size $selectedTextSize"
            textSize.progress = 18
        }

        textSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                textPreview.text = "Text size $p1"
                textPreview.textSize = p1.toFloat()
                selectedTextSize = p1
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.setPositiveButton("OK") { dialog, _ ->
                changeTextSize(editText, selectedTextSize)
                dialog.dismiss()
            }
        builder.create().show()
    }

    //thay doi co chu
    private fun changeTextSize(editText: EditText, size: Int) {
        val start = binding.edtContent.selectionStart
        val end = binding.edtContent.selectionEnd

        if (start < end) {
            val spannable = editText.text as Spannable
            spannable.setSpan(
                AbsoluteSizeSpan(size, true),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    //chuyen ma mau tu String sang Int
    private fun parseColor(colorString: String): Int {
        return Color.parseColor(colorString)
    }

    //hien thi hop thoai chon mau nen hoac mau chu cho edit text
    private fun showFormatColorPickerDialog(imageView: ImageView) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_format_color_text, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rcvFormatColor)
        val removeColor = dialogView.findViewById<Button>(R.id.removeFormatColorBtn)
        var isRemove = false

        recyclerView.layoutManager = GridLayoutManager(this, 8)
        colorAdapter = ListColorAdapter(formatColor, this)
        recyclerView.adapter = colorAdapter
        removeColor.setOnClickListener { isRemove = true }

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }.setPositiveButton("OK") { dialog, _ ->
                if (isRemove) selectedFormatTextColor = null
                setupColorForEditText(binding.edtContent, imageView)
                dialog.dismiss()
            }
        builder.create().show()

    }

    //thiet lap mau nen hoac mau chu cho edit text
    private fun setupColorForEditText(editText: EditText, imageView: ImageView) {
        selectedFormatTextColor.let { color ->
            if (selectedFormatTextColor.isNullOrEmpty()) {
                changeColorSelected(editText, "#000000", imageView)
            } else {
                changeColorSelected(editText, color, imageView)
            }
        }
    }

    //chinh sua van ban theo mau da duoc chon
    private fun changeColorSelected(editText: EditText, color: String?, imageView: ImageView) {
        val start = editText.selectionStart
        val end = editText.selectionEnd
        val spannable = editText.text as Spannable
        val colorInt = parseColor(color!!)

        if (imageView == binding.fillColor) {
            val spans = spannable.getSpans(start, end, BackgroundColorSpan::class.java)
            if (spans.isNotEmpty()) {
                //xoa bo mau nen da co
                for (span in spans) {
                    spannable.removeSpan(span)
                }
            } else {
                //them mau nen neu chua co
                spannable.setSpan(
                    BackgroundColorSpan(colorInt),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            Log.d("TAG", "setupColorForEditText: fill")
        }

        if (imageView == binding.textColor) {
            val spans = spannable.getSpans(start, end, ForegroundColorSpan::class.java)
            if (spans.isNotEmpty()) {
                //xoa bo mau chu da co
                for (span in spans) {
                    spannable.removeSpan(span)
                }
            } else {
                //them mau chu neu chua co
                spannable.setSpan(
                    ForegroundColorSpan(colorInt),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            Log.d("TAG", "setupColorForEditText: text")
        }
    }

    //hien thi hop thoai chon mau note
    private fun showColorPickerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_colorize, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rcvColor)
        val removeColor = dialogView.findViewById<Button>(R.id.removeColorBtn)
        var isRemove = false

        recyclerView.layoutManager = GridLayoutManager(this, 5)
        colorAdapter = ListColorAdapter(colors, this)
        recyclerView.adapter = colorAdapter

        removeColor.setOnClickListener {
            isRemove = true
        }

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }.setPositiveButton("OK") { dialog, which ->
                if (isRemove) selectedColor = null
                handleOkButtonClick()
                dialog.dismiss()
            }

        builder.create().show()

    }

    //to mau note
    @SuppressLint("NotifyDataSetChanged", "ResourceAsColor")
    private fun handleOkButtonClick() {
        selectedColor.let { color ->
            if (selectedColor.isNullOrEmpty()) {
                binding.editNote.setBackgroundResource(R.drawable.bg_edit_note)
            } else {
                val backgroundDrawable = GradientDrawable()
                backgroundDrawable.setColor(Color.parseColor(color ?: "#FFFFFF"))
                backgroundDrawable.setStroke(4, R.color.brown)
                binding.editNote.background = backgroundDrawable
            }
            val id = intent.getIntExtra("id", 0)
            val title = intent.getStringExtra("title")
            val content = intent.getStringExtra("content")
            val created = intent.getStringExtra("created")
            val time = intent.getStringExtra("time")
            val noteId = if (id ==0) noteViewModel.getLatestId() else id

            val note =
                Note(
                    noteId,
                    title.toString(),
                    content.toString(),
                    time.toString(),
                    created.toString(),
                    color,
                    false
                )
            Log.d("TAG", "handleOkButtonClick: $note")
            noteViewModel.updateNote(note)
        }

        noteAdapter.notifyDataSetChanged()
    }

    override fun onColorClick(color: String) {
        selectedColor = color
        selectedFormatTextColor = color
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
            binding.edtContent.text.trim().split("\\s+".toRegex())
                .filter { it.isNotBlank() }.size
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
                val created = intent.getStringExtra("created")
                val time = intent.getStringExtra("time")

                val note =
                    Note(
                        id,
                        title.toString(),
                        content.toString(),
                        time.toString(),
                        created.toString(),
                        null,
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
        val noteId = if (id == 0) noteViewModel.getLatestId() else id
        val color = noteViewModel.getColor(noteId)

        val noteTitle = binding.edtTitle.text.toString()
        val noteContent = Html.toHtml(binding.edtContent.text)
        val note =
            Note(noteId, noteTitle, noteContent, getCurrentTime(), created!!, color, false)
        noteViewModel.updateNote(note)
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
        noteViewModel =
            ViewModelProvider(this, viewModelProviderFactory)[NoteViewModel::class.java]

        val categoryRepository = CategoryRepository(NoteDatabase(this))
        val cateViewModelProviderFactory =
            CategoryViewModelFactory(application, categoryRepository)
        categoryViewModel =
            ViewModelProvider(this, cateViewModelProviderFactory)[CategoryViewModel::class.java]

        val noteCategoryRepository = NoteCategoryRepository(NoteDatabase(this))
        val noteCategoryViewModelFactory =
            NoteCategoryViewModelFactory(application, noteCategoryRepository)
        noteCategoryViewModel =
            ViewModelProvider(
                this,
                noteCategoryViewModelFactory
            )[NoteCategoryViewModel::class.java]

        noteAdapter = ListNoteAdapter(this, object : OnItemClickListener {
            override fun onNoteClick(note: Note, isChoose: Boolean) {
            }

            override fun onNoteLongClick(note: Note) {
            }
        })

        categoryAdapter = ListCategoryAdapter(this)
        colorAdapter = ListColorAdapter(colors, this)

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