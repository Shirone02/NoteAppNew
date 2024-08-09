package com.example.noteapp.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.R
import com.example.noteapp.activities.EditNoteActivity
import com.example.noteapp.activities.MainActivity
import com.example.noteapp.adapter.ListCategoryAdapter
import com.example.noteapp.adapter.ListNoteAdapter
import com.example.noteapp.databinding.FragmentNoteWithCategoryBinding
import com.example.noteapp.listeners.OnItemClickListener
import com.example.noteapp.models.Category
import com.example.noteapp.models.Note
import com.example.noteapp.models.NoteCategoryCrossRef
import com.example.noteapp.viewmodel.CategoryViewModel
import com.example.noteapp.viewmodel.NoteCategoryViewModel
import com.example.noteapp.viewmodel.NoteViewModel

class NoteWithCategoryFragment : Fragment(), MenuProvider, SearchView.OnQueryTextListener {

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
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

        activity?.let {
            noteViewModel.getNotesByCategory(categoryId).observe(viewLifecycleOwner) { note ->
                noteAdapter.differ.submitList(note)
                currentList = noteAdapter.differ.currentList
                updateUI(note)
            }
        }

        activity?.let {
            categoryViewModel.getAllCategory().observe(viewLifecycleOwner) { category ->
                categoryAdapter.differ.submitList(category)
                categories = categoryAdapter.differ.currentList
            }
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
        noteViewModel.deleteNotes(selectedIds)
        noteAdapter.removeSelectedItems()
    }

    private fun showOptionDialog() {
        val sortOption = arrayOf(
            "edit date: from newest",
            "edit date: from oldest",
            "title: A to Z",
            "title: Z to A",
            "creation date: from newest",
            "creation date: from oldest"
        )

        var selectedOption = 0
        val noteList = noteAdapter.differ.currentList
        val builder: androidx.appcompat.app.AlertDialog.Builder =
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Sort by")
            .setPositiveButton("Sort") { dialog, which ->
                when (selectedOption) {
                    0 -> sortByEditDateNewest(noteList)
                    1 -> sortByEditDateOldest(noteList)
                    2 -> sortByTitleAToZ(noteList)
                    3 -> sortByTitleZToA(noteList)
                    4 -> sortByCreationDateNewest(noteList)
                    5 -> sortByCreationDateOldest(noteList)
                }
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .setSingleChoiceItems(sortOption, selectedOption) { dialog, which ->
                selectedOption = which
            }

        builder.create().show()
    }

    private fun sortByCreationDateOldest(noteList: List<Note>) {
        return noteAdapter.differ.submitList(noteList.sortedBy { it.id })
    }

    private fun sortByCreationDateNewest(noteList: List<Note>) {
        return noteAdapter.differ.submitList(noteList.sortedByDescending { it.id })
    }

    private fun sortByTitleZToA(noteList: List<Note>) {
        return noteAdapter.differ.submitList(noteList.sortedByDescending { it.title })
    }

    private fun sortByTitleAToZ(noteList: List<Note>) {
        return noteAdapter.differ.submitList(noteList.sortedBy { it.title })
    }

    private fun sortByEditDateOldest(noteList: List<Note>) {
        return noteAdapter.differ.submitList(noteList.sortedBy { it.time })
    }

    private fun sortByEditDateNewest(noteList: List<Note>) {
        return noteAdapter.differ.submitList(noteList.sortedByDescending { it.time })
    }

    private fun searchNote(query: String?) {
        if (query != null) {
            if (query.isEmpty()) {
                noteAdapter.differ.submitList(currentList)
            } else {
                noteViewModel.searchNote("%$query%").observe(this) { notes ->
                    noteAdapter.differ.submitList(notes)
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
            noteAdapter.differ.submitList(currentList)
        } else {
            searchNote(newText)
        }
        return true
    }

}