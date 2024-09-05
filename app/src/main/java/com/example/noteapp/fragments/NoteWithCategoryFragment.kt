package com.example.noteapp.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.activities.EditNoteActivity
import com.example.noteapp.activities.MainActivity
import com.example.noteapp.adapter.ListCategoryAdapter
import com.example.noteapp.adapter.ListColorAdapter
import com.example.noteapp.adapter.ListNoteAdapter
import com.example.noteapp.databinding.FragmentNoteWithCategoryBinding
import com.example.noteapp.listeners.OnColorClickListener
import com.example.noteapp.listeners.OnItemClickListener
import com.example.noteapp.models.Category
import com.example.noteapp.models.Note
import com.example.noteapp.models.NoteCategoryCrossRef
import com.example.noteapp.viewmodel.CategoryViewModel
import com.example.noteapp.viewmodel.NoteCategoryViewModel
import com.example.noteapp.viewmodel.NoteViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NoteWithCategoryFragment : Fragment(), MenuProvider, SearchView.OnQueryTextListener,
    OnColorClickListener {

    private val binding: FragmentNoteWithCategoryBinding by lazy {
        FragmentNoteWithCategoryBinding.inflate(layoutInflater)
    }

    private var categoryId: Int = 0
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var noteCategoryViewModel: NoteCategoryViewModel
    private lateinit var noteAdapter: ListNoteAdapter
    private lateinit var categoryAdapter: ListCategoryAdapter
    private lateinit var uncategorizedView: View
    private lateinit var categories: List<Category>
    private lateinit var currentList: List<Note>
    private var isAlternateMenuVisible: Boolean = false
    val list = ArrayList<Note>()
    private var sortedList = mutableListOf<Note>()
    var lastId = 1
    private var selectedColor: String? = null
    private lateinit var colorAdapter: ListColorAdapter
    private val colors = listOf(
        "#FFCDD2", "#F8BBD0", "#E1BEE7", "#D1C4E9", "#C5CAE9",
        "#BBDEFB", "#B3E5FC", "#B2EBF2", "#B2DFDB", "#C8E6C9",
        "#DCEDC8", "#F0F4C3", "#FFECB3", "#FFE0B2", "#FFCCBC",
        "#D7CCC8", "#F5F5F5", "#CFD8DC", "#FF8A80", "#FF80AB"
    )
    companion object {
        private const val READ_FILE_REQUEST_CODE = 101
        private const val REQUEST_WRITE_PERMISSION = 1001
        private const val REQUEST_CODE_PICK_DIRECTORY = 1002
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        setHasOptionsMenu(true)

        noteViewModel = (activity as MainActivity).noteViewModel
        categoryViewModel = (activity as MainActivity).categoryViewModel
        noteCategoryViewModel = (activity as MainActivity).noteCategoryViewModel
        uncategorizedView = view

        categoryId = arguments?.getInt("categoryId") ?: 0

        setUpNoteRecyclerView()

    }

    //hien thi hop thoai chon cac the loai
    private fun showCategorizeDialog() {
        val checkedItem = BooleanArray(categories.size)

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("Select category")
            .setPositiveButton("OK") { dialog, which ->
                val selectedCategories = mutableListOf<Category>()
                for (i in categories.indices) {
                    if (checkedItem[i]) {
                        selectedCategories.add(categories[i])
                    }
                }

                // Tạo danh sách NoteCategoryCrossRef để liên kết note với category
                val noteCategoryCrossRefs = mutableListOf<NoteCategoryCrossRef>()
                val selectedNotes = noteAdapter.getSelectedItems()

                for (noteId in selectedNotes.map { it.id }) {
                    for (categoryId in selectedCategories.map { it.id }) {
                        noteCategoryCrossRefs.add(NoteCategoryCrossRef(noteId, categoryId))
                    }
                }
                // Chèn danh sách NoteCategoryCrossRef vào cơ sở dữ liệu
                noteCategoryViewModel.addListNoteCategory(noteCategoryCrossRefs)
                Toast.makeText(
                    requireContext(),
                    "Notes and categories linked successfully",
                    Toast.LENGTH_SHORT
                ).show()

                //thay doi lai menu
                isAlternateMenuVisible = !isAlternateMenuVisible
                changeDrawerNavigationIcon()
                requireActivity().invalidateOptionsMenu()
                updateSelectedCount()

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

    private fun setUpNoteRecyclerView() {
        noteAdapter = ListNoteAdapter(requireContext(), object : OnItemClickListener {
            override fun onNoteClick(note: Note, isChoose: Boolean) {
                if (!isChoose && !isAlternateMenuVisible) {
                    val intent = Intent(activity, EditNoteActivity::class.java)
                    intent.putExtra("id", note.id)
                    intent.putExtra("title", note.title)
                    intent.putExtra("content", note.content)
                    intent.putExtra("created", note.created)
                    intent.putExtra("time", note.time)
                    intent.putExtra("color", note.color)
                    startActivity(intent)
                }

                if (isAlternateMenuVisible) {
                    updateSelectedCount()
                }
            }

            override fun onNoteLongClick(note: Note) {
                isAlternateMenuVisible = true
                requireActivity().invalidateOptionsMenu()
                if (isAlternateMenuVisible) {
                    changeBackNavigationIcon()
                    updateSelectedCount()
                }
            }
        })

        categoryAdapter = ListCategoryAdapter(requireContext())

        binding.noteWithCategoryRcv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.noteWithCategoryRcv.adapter = noteAdapter

//        activity?.let {
//            noteViewModel.getNotesByCategory(categoryId).observe(viewLifecycleOwner) { note ->
//                noteAdapter.differ.submitList(note)
//                currentList = noteAdapter.differ.currentList
//                updateUI(note)
//            }
//        }

        updateListNote()

        activity?.let {
            categoryViewModel.getAllCategory().observe(viewLifecycleOwner) { category ->
                categoryAdapter.differ.submitList(category)
                categories = categoryAdapter.differ.currentList
            }
        }
    }

    private fun updateListNote() {
        val cateRef = FirebaseDatabase.getInstance().getReference("note_cate")
            .child(FirebaseAuth.getInstance().currentUser!!.uid).child(categoryId.toString())

        cateRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                if (snapshot.exists()) {
                    lastId = snapshot.children.last().key?.toIntOrNull() ?: 0
                    for (issue in snapshot.children) {
                        if (issue.getValue(Note::class.java)!!.color.isNullOrEmpty()) {
                            issue.getValue(Note::class.java)!!.color = null
                        }
                        list.add(issue.getValue(Note::class.java)!!)
                    }
                    sortedList = list.toMutableList()
                    updateRecyclerView()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun updateRecyclerView() {
        if (sortedList.isNotEmpty()) {
            binding.noteWithCategoryRcv.layoutManager = GridLayoutManager(context, 1)
            noteAdapter.differ.submitList(null)
            noteAdapter.differ.submitList(sortedList)
            binding.noteWithCategoryRcv.adapter = noteAdapter
        }
    }

    private fun updateSelectedCount() {
        (activity as MainActivity).let { mainActivity ->
            val toolbar = mainActivity.findViewById<Toolbar>(R.id.topAppBar)
            if (isAlternateMenuVisible) {
                toolbar.setTitle(noteAdapter.getSelectedItemsCount().toString())
            } else {
                toolbar.setTitle("Notepad Free")
            }
        }
    }

    private fun changeBackNavigationIcon() {
        (activity as MainActivity).let { mainActivity ->
            val toolbar = mainActivity.findViewById<Toolbar>(R.id.topAppBar)
            val drawerLayout = mainActivity.findViewById<DrawerLayout>(R.id.drawerLayout)

            toolbar.setNavigationIcon(R.drawable.ic_back)
            toolbar.navigationIcon?.setTint(ContextCompat.getColor(requireContext(), R.color.white))
            toolbar.setNavigationOnClickListener {
                noteAdapter.isChoosing = false
                activity?.invalidateOptionsMenu()
                isAlternateMenuVisible = !isAlternateMenuVisible
                clearSelection()
                toolbar.setNavigationIcon(R.drawable.ic_option)
                toolbar.navigationIcon?.setTint(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                toolbar.setNavigationOnClickListener {
                    drawerLayout.openDrawer(GravityCompat.START)
                }
                toolbar.setTitle("Notepad Free")
            }
        }
    }

    //thay doi drawer navigation icon
    private fun changeDrawerNavigationIcon() {
        (activity as MainActivity).let { mainActivity ->
            val toolbar = mainActivity.findViewById<Toolbar>(R.id.topAppBar)
            val drawerLayout = mainActivity.findViewById<DrawerLayout>(R.id.drawerLayout)

            isAlternateMenuVisible = false
            activity?.invalidateOptionsMenu()
            clearSelection()
            toolbar.setNavigationIcon(R.drawable.ic_option)
            toolbar.navigationIcon?.setTint(ContextCompat.getColor(requireContext(), R.color.white))
            toolbar.setNavigationOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }
            toolbar.setTitle("Notepad Free")
        }
    }

    private fun clearSelection() {
        noteAdapter.clearSelection()
    }

    private fun updateUI(note: List<Note>) {
        if (note.isNotEmpty()) {
            binding.noteWithCategoryRcv.visibility = View.VISIBLE
        } else {
            binding.noteWithCategoryRcv.visibility = View.GONE
        }
    }

    private fun showDeleteDialog() {
        val builder: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog.Builder(requireContext())

        builder.setTitle("Delete")
            .setMessage("Do you want to delete?")
            .setPositiveButton("Delete") { dialog, which ->
                deleteSelectedItem()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun deleteSelectedItem() {
        val selectedNotes = noteAdapter.getSelectedItems()
        val selectedIds = selectedNotes.map { it.id }

        val cateRef = FirebaseDatabase.getInstance().getReference("note_cate")
            .child(FirebaseAuth.getInstance().currentUser!!.uid).child(categoryId.toString())
        selectedIds.forEach { id ->
            cateRef.child(id.toString()).removeValue()
        }
        noteAdapter.removeSelectedItems()
        updateSelectedCount()
    }

    private fun showOptionDialog() {
        val sortOption = arrayOf(
            "edit date: from newest",
            "edit date: from oldest",
            "title: A to Z",
            "title: Z to A",
        )

        var selectedOption = 0

        val builder: androidx.appcompat.app.AlertDialog.Builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Sort by")
            .setPositiveButton("Sort") { dialog, which ->
                when (selectedOption) {
                    0 -> sortByEditDateNewest()
                    1 -> sortByEditDateOldest()
                    2 -> sortByTitleAToZ()
                    3 -> sortByTitleZToA()
                }
                updateRecyclerView()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .setSingleChoiceItems(sortOption, selectedOption) { dialog, which ->
                selectedOption = which
                if (selectedOption == 4 || selectedOption == 5) {
                    noteAdapter.isCreated = true
                } else {
                    noteAdapter.isCreated = true
                }
            }

        builder.create().show()
    }

    private fun sortByTitleZToA() {
        sortedList = list.sortedByDescending { it.title.toLowerCase() }.toMutableList()
    }

    private fun sortByTitleAToZ() {
        sortedList = list.sortedBy { it.title.toLowerCase() }.toMutableList()
    }

    private fun sortByEditDateOldest() {
        sortedList = list.sortedBy { it.time.length }.toMutableList()
    }

    private fun sortByEditDateNewest() {
        sortedList = list.sortedByDescending { it.time.length }.toMutableList()
    }

    private fun searchNote(query: String?) {
        val searchList = ArrayList<Note>()
        if (query != null) {
            if (query.isEmpty()) {
                noteAdapter.differ.submitList(null)
                noteAdapter.differ.submitList(list)
            } else {
                for (it in list) {
                    if (it.title.toLowerCase().contains(query.toLowerCase())) {
                        searchList.add(it)
                    }
                    noteAdapter.differ.submitList(null)
                    noteAdapter.differ.submitList(searchList)
                }
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(
            if (isAlternateMenuVisible) {
                R.menu.menu_selection
            } else {
                R.menu.top_app_bar
            }, menu
        )

        if (!isAlternateMenuVisible) {
            val menuSearch = menu.findItem(R.id.search).actionView as SearchView
            menuSearch.isSubmitButtonEnabled = false
            menuSearch.setOnQueryTextListener(this)

            val sort = SpannableString(menu.findItem(R.id.sort).title)
            sort.setSpan(ForegroundColorSpan(Color.WHITE), 0, sort.length, 0)
            menu.findItem(R.id.sort).title = sort
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.sort -> {
                showOptionDialog()
                true
            }

            R.id.selectAll -> {
                noteAdapter.selectAllItem()
                isAlternateMenuVisible = true
                changeBackNavigationIcon()
                requireActivity().invalidateOptionsMenu()
                updateSelectedCount()
                true
            }

            R.id.delete -> {
                showDeleteDialog()
                true
            }

            R.id.categorize -> {
                showCategorizeDialog()
                true
            }

            R.id.export_notes_to_text_file -> {
                selectDirectory()
                true
            }

            R.id.import_text_files -> {
                openTextFile()
                true
            }

            R.id.Colorize -> {
                showColorPickerDialog()
                true
            }
            else -> {
                false
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText.isNullOrEmpty()) {
            noteAdapter.differ.submitList(null)
            noteAdapter.differ.submitList(list)
        } else {
            searchNote(newText)
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_WRITE_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                selectDirectory()
            } else {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun openTextFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain" // Loại tệp văn bản
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, READ_FILE_REQUEST_CODE)
    }

    private fun selectDirectory() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, REQUEST_CODE_PICK_DIRECTORY)
    }

    @SuppressLint("InflateParams")
    private fun showColorPickerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_colorize, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rcvColor)
        val removeColor = dialogView.findViewById<Button>(R.id.removeColorBtn)
        var isRemove = false

        recyclerView.layoutManager = GridLayoutManager(context, 5)
        colorAdapter = ListColorAdapter(colors, this)
        recyclerView.adapter = colorAdapter

        removeColor.setOnClickListener {
            isRemove = true
        }

        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
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

    @SuppressLint("NotifyDataSetChanged")
    private fun handleOkButtonClick() {
        val database = FirebaseDatabase.getInstance()
        val mAuth = FirebaseAuth.getInstance()
        val selectedNotes = noteAdapter.getSelectedItems()
        val cateRef = FirebaseDatabase.getInstance().getReference("note_cate")
            .child(FirebaseAuth.getInstance().currentUser!!.uid).child(categoryId.toString())

        selectedColor.let { color ->
            if (selectedColor.isNullOrEmpty()) {
                selectedNotes.forEach { note ->
                    note.color = color
                    Log.d("TAG", "handleOkButtonClick: ${note.color}")
                    cateRef.child((note.id).toString()).setValue(note)
                }
            } else {
                selectedNotes.forEach { note ->
                    note.color = color
                    Log.d("TAG", "handleOkButtonClick: ${note.color}")
                    cateRef.child((note.id).toString()).setValue(note)
                }
            }
        }

        noteAdapter.notifyDataSetChanged()
        isAlternateMenuVisible = !isAlternateMenuVisible
        changeDrawerNavigationIcon()
        requireActivity().invalidateOptionsMenu()
        updateSelectedCount()
    }

    override fun onColorClick(color: String) {
        selectedColor = color
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PICK_DIRECTORY && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            // Lưu Uri bằng cách sử dụng quyền có thể duy trì
            uri?.let {
                exportNoteToTextFile(it)
            }
        }

        if (requestCode == READ_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedFile = mutableListOf<Uri>()

            data?.clipData?.let { clipData ->
                //neu nguoi dung chon nhieu tep
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    selectedFile.add(uri)
                }
            } ?: run {
                // neu nguoi dung chi chon 1 tep
                data?.data?.let { uri ->
                    Log.d("TAG", "onActivityResult: $uri")
                    selectedFile.add(uri)
                }
            }

            //xu li danh sach cac tep da chon
            handleSelectedFiles(selectedFile)
            Toast.makeText(context, "${selectedFile.size} note(s) added", Toast.LENGTH_SHORT).show()
        }
    }

    //ham xu ly tep
    private fun handleSelectedFiles(uris: List<Uri>) {
        for (uri in uris) {
            val note = createNoteFromTextFile(uri)
            val cateRef = FirebaseDatabase.getInstance().getReference("note_cate")
                .child(FirebaseAuth.getInstance().currentUser!!.uid).child(categoryId.toString())
            cateRef.child((note.id.toString()).toString()).setValue(note)
//            noteViewModel.addNote(note)
        }
        updateListNote()
    }

    private fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()

        val formattedDate =
            SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault()).format(calendar.time)

        return formattedDate
    }

    //tao note tu file txt
    private fun createNoteFromTextFile(uri: Uri): Note {
        val content = readTextTxt(uri)
        val title = getFileName(uri)
        val newId = if (lastId == 0) 1 else lastId + 1

        //tao note moi
        val note = Note(newId, title!!, content, getCurrentTime(), getCurrentTime(), null, false)
        return note
    }

    //lay ten tep
    private fun getFileName(uri: Uri): String? {
        val contentResolver = requireContext().contentResolver
        var fileTxtName: String? = null

        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val fileName =
                    if (displayNameIndex != -1) it.getString(displayNameIndex) else "Unknown"
                fileTxtName = fileName
            }
        }
        return fileTxtName
    }

    //doc noi dung tu file txt
    private fun readTextTxt(uri: Uri): String {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?
        try {
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line).append("\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return stringBuilder.toString()
    }

    private fun exportNoteToTextFile(uri: Uri) {
        var selectedNotes = noteAdapter.getSelectedItems()
        if (selectedNotes.isEmpty()) {
            selectedNotes = noteAdapter.differ.currentList.toSet()
        }
        selectedNotes.forEach { note ->
            val fileName = "${note.title}.txt"
            createFile(uri, fileName, note.content)
        }
        Toast.makeText(
            requireContext(),
            "${selectedNotes.size} note(s) exported",
            Toast.LENGTH_SHORT
        )
            .show()
    }
    private fun createFile(uri: Uri, fileName: String, content: String) {
        try {
            val documentUri = DocumentsContract.buildDocumentUriUsingTree(
                uri,
                DocumentsContract.getTreeDocumentId(uri)
            )
            val docUri = DocumentsContract.createDocument(
                requireContext().contentResolver,
                documentUri,
                "text/plain",
                fileName
            )
            docUri?.let {
                requireContext().contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(content.toByteArray())
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}