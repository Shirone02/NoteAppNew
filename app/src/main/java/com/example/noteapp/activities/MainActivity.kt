package com.example.noteapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.noteapp.R
import com.example.noteapp.adapter.ListNoteAdapter
import com.example.noteapp.database.NoteDatabase
import com.example.noteapp.databinding.ActivityMainBinding
import com.example.noteapp.fragments.BackupFragment
import com.example.noteapp.fragments.EditCategoriesFragment
import com.example.noteapp.fragments.HelpFragment
import com.example.noteapp.fragments.NotesFragment
import com.example.noteapp.fragments.PrivacyPolicyFragment
import com.example.noteapp.fragments.RateFragment
import com.example.noteapp.fragments.SettingFragment
import com.example.noteapp.fragments.TrashFragment
import com.example.noteapp.fragments.UncategorizedFragment
import com.example.noteapp.listeners.OnItemClickListener
import com.example.noteapp.models.Note
import com.example.noteapp.repository.CategoryRepository
import com.example.noteapp.repository.NoteRepository
import com.example.noteapp.viewmodel.CategoryViewModel
import com.example.noteapp.viewmodel.CategoryViewModelFactory
import com.example.noteapp.viewmodel.NoteViewModel
import com.example.noteapp.viewmodel.NoteViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import java.util.ArrayList
import java.util.Calendar


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    lateinit var noteViewModel: NoteViewModel
    lateinit var categoryViewModel: CategoryViewModel
    private lateinit var noteAdapter: ListNoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setUpViewModel()

        setUpNoteRecyclerView()

        //setSupportActionBar(binding.topAppBar)
//
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.topAppBar,
            R.string.open_nav,
            R.string.close_nav
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NotesFragment()).commit()
            binding.navView.setCheckedItem(R.id.nav_note)
        }

        binding.topAppBar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.white))

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.search -> {
                    val searchView = menuItem.actionView as SearchView
                    val currentList = noteAdapter.differ.currentList

                    searchView.setOnQueryTextListener(object : OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            if (newText.isNullOrEmpty()) {
                                noteAdapter.differ.submitList(currentList)
                            } else {
                                //filterList(newText)
                                val fragment =
                                    supportFragmentManager.findFragmentById(R.id.fragment_container) as NotesFragment
                                fragment.searchNote(newText)
                            }
                            return true
                        }

                    })
                    true
                }

                R.id.sort -> {
                    //showOptionDialog()
                    try {
                        val fragment =
                            supportFragmentManager.findFragmentById(R.id.fragment_container) as NotesFragment
                        fragment.showOptionDialog()
                    } catch (_: Exception) {
                    }
                    true
                }

                R.id.delete -> {
                    //showDeleteDialog()
                    val fragment =
                        supportFragmentManager.findFragmentById(R.id.fragment_container) as NotesFragment
                    fragment.showDeleteDialog()
                    true
                }

                R.id.selectAll -> {
                    //noteAdapter.selectAllItem()
                    val fragment =
                        supportFragmentManager.findFragmentById(R.id.fragment_container) as NotesFragment
                    fragment.noteAdapter.selectAllItem()
                    true
                }

                R.id.categorize -> {
                    val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as UncategorizedFragment
                    fragment.showCategorizeDialog()
                    true
                }

                else -> {
                    false
                }
            }
        }

    }

//    private fun showDeleteDialog() {
//        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
//
//        builder.setTitle("Delete")
//            .setMessage("Do you want to delete?")
//            .setPositiveButton("Delete") { dialog, which ->
//                deleteSelectedItem()
//                dialog.dismiss()
//            }
//            .setNegativeButton("Cancel") { dialog, which ->
//                dialog.dismiss()
//            }
//        builder.create().show()
//    }
//
//    private fun deleteSelectedItem() {
//        val selectedNotes = noteAdapter.getSelectedItems()
//        val selectedIds = selectedNotes.map { it.id }
//        noteViewModel.deleteNotes(selectedIds)
//        noteAdapter.removeSelectedItems()
//    }

    fun showDeleteIcon() {
        val fragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NotesFragment

        binding.topAppBar.menu.clear()
        binding.topAppBar.inflateMenu(R.menu.menu_selection)
        binding.topAppBar.setTitle("${fragment.noteAdapter.getSelectedItemsCount()}")
        binding.topAppBar.setNavigationIcon(R.drawable.ic_back)
        binding.topAppBar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.white))
        binding.topAppBar.setNavigationOnClickListener {
            fragment.clearSelection()
            binding.topAppBar.menu.clear()
            binding.topAppBar.inflateMenu(R.menu.top_app_bar)
            binding.topAppBar.setNavigationIcon(R.drawable.ic_option)
            binding.topAppBar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.white))
            binding.topAppBar.setNavigationOnClickListener {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
            binding.topAppBar.setTitle("Notepad Free")
        }
    }

//    private fun showOptionDialog() {
//        val sortOption = arrayOf(
//            "edit date: from newest",
//            "edit date: from oldest",
//            "title: A to Z",
//            "title: Z to A",
//            "creation date: from newest",
//            "creation date: from oldest"
//        )
//
//        var selectedOption = 0
//        val noteList = noteAdapter.differ.currentList
//        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
//        builder.setTitle("Sort by")
//            .setPositiveButton("Sort") { dialog, which ->
//                when (selectedOption) {
//                    0 -> sortByEditDateNewest(noteList)
//                    1 -> sortByEditDateOldest(noteList)
//                    2 -> sortByTitleAToZ(noteList)
//                    3 -> sortByTitleZToA(noteList)
//                    4 -> sortByCreationDateNewest(noteList)
//                    5 -> sortByCreationDateOldest(noteList)
//                }
//            }
//            .setNegativeButton("Cancel") { dialog, which ->
//                dialog.dismiss()
//            }
//            .setSingleChoiceItems(sortOption, selectedOption) { dialog, which ->
//                selectedOption = which
//            }
//
//        builder.create().show()
//    }

//    private fun sortByCreationDateOldest(noteList: List<Note>) {
//        return noteAdapter.differ.submitList(noteList.sortedBy { it.id })
//    }
//
//    private fun sortByCreationDateNewest(noteList: List<Note>) {
//        return noteAdapter.differ.submitList(noteList.sortedByDescending { it.id })
//    }
//
//    private fun sortByTitleZToA(noteList: List<Note>) {
//        return noteAdapter.differ.submitList(noteList.sortedByDescending { it.title })
//    }
//
//    private fun sortByTitleAToZ(noteList: List<Note>) {
//        return noteAdapter.differ.submitList(noteList.sortedBy { it.title })
//    }
//
//    private fun sortByEditDateOldest(noteList: List<Note>) {
//        return noteAdapter.differ.submitList(noteList.sortedBy { it.time })
//    }
//
//    private fun sortByEditDateNewest(noteList: List<Note>) {
//        return noteAdapter.differ.submitList(noteList.sortedByDescending { it.time })
//    }
//
//    private fun filterList(newText: String?) {
//        noteViewModel.searchNote("%" + newText!! + "%").observe(this) { notes ->
//            noteAdapter.differ.submitList(notes)
//        }
//    }

//    // thêm note
//    private fun addNote() {
//        val note = Note(0, "Untitled", "", getCurrentTime(), null)
//        noteViewModel.addNote(note)
//
//        val intent = Intent(this@MainActivity, EditNoteActivity::class.java)
//        intent.putExtra("id", note.id)
//        intent.putExtra("title", note.title)
//        intent.putExtra("content", note.content)
//        intent.putExtra("categoryId", note.categoryId)
//        startActivity(intent)
//
//        Toast.makeText(this, "Add successful !!!", Toast.LENGTH_SHORT).show()
//    }

    //hiển thị recyclerView danh sách note
    private fun setUpNoteRecyclerView() {
        noteAdapter = ListNoteAdapter(object : OnItemClickListener {
            override fun onNoteClick(note: Note, isChoose: Boolean) {
//                if (!isChoose) {
//                    val intent = Intent(this@MainActivity, EditNoteActivity::class.java)
//                    intent.putExtra("id", note.id)
//                    intent.putExtra("title", note.title)
//                    intent.putExtra("content", note.content)
//                    intent.putExtra("categoryId", note.categoryId)
//                    startActivity(intent)
//                }
            }

            override fun onNoteLongClick(note: Note) {
                //showDeleteIcon()
            }
        })

//        binding.listNoteRecyclerView.layoutManager =
//            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
//        binding.listNoteRecyclerView.adapter = noteAdapter
//
//        this.let {
//            noteViewModel.getAllNote().observe(this) { note ->
//                noteAdapter.differ.submitList(note)
//                //updateUI(note)
//                binding.listNoteRecyclerView.visibility = View.GONE
//            }
//        }
//    }
//
//    private fun updateUI(note: List<Note>?) {
//        if (note != null) {
//            if (note.isNotEmpty()) {
//                binding.listNoteRecyclerView.visibility = View.VISIBLE
//            } else {
//                binding.listNoteRecyclerView.visibility = View.GONE
//            }
//        }

    }

    private fun setUpViewModel() {
        val noteRepository = NoteRepository(NoteDatabase(this))
        val categoryRepository = CategoryRepository(NoteDatabase(this))

        val categoryViewModelProviderFactory =
            CategoryViewModelFactory(application, categoryRepository)
        val viewModelProviderFactory = NoteViewModelFactory(application, noteRepository)

        categoryViewModel =
            ViewModelProvider(this, categoryViewModelProviderFactory)[CategoryViewModel::class.java]
        noteViewModel = ViewModelProvider(this, viewModelProviderFactory)[NoteViewModel::class.java]
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_note -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NotesFragment()).commit()

            R.id.nav_edit_categories -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EditCategoriesFragment()).commit()

            R.id.nav_backup -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BackupFragment()).commit()

            R.id.nav_trash -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TrashFragment()).commit()

            R.id.nav_uncategorized -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UncategorizedFragment()).commit()

            R.id.nav_setting -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingFragment()).commit()

            R.id.nav_rate -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RateFragment()).commit()

            R.id.nav_help -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HelpFragment()).commit()

            R.id.nav_policy -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PrivacyPolicyFragment()).commit()
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}