package com.example.noteapp.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.R
import com.example.noteapp.activities.EditNoteActivity
import com.example.noteapp.activities.MainActivity
import com.example.noteapp.adapter.ListNoteAdapter
import com.example.noteapp.databinding.FragmentNotesBinding
import com.example.noteapp.listeners.OnItemClickListener
import com.example.noteapp.models.Note
import com.example.noteapp.viewmodel.NoteViewModel
import java.util.Calendar


class NotesFragment : Fragment(R.layout.fragment_notes), MenuProvider {

    private val binding: FragmentNotesBinding by lazy {
        FragmentNotesBinding.inflate(layoutInflater)
    }

    private lateinit var noteViewModel: NoteViewModel
    lateinit var noteAdapter: ListNoteAdapter
    private lateinit var currentList: List<Note>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val menuHost: MenuHost = requireActivity()
//        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        noteViewModel = (activity as MainActivity).noteViewModel

        setupNoteRecyclerView()

        currentList = noteAdapter.differ.currentList

        binding.addNoteFab.setOnClickListener { addNote() }

    }

    private fun addNote() {
        val note = Note(0, "Untitled", "", getCurrentTime(), null)
        noteViewModel.addNote(note)

        val intent = Intent(context, EditNoteActivity::class.java)
        intent.putExtra("id", note.id)
        intent.putExtra("title", note.title)
        intent.putExtra("content", note.content)
        intent.putExtra("categoryId", note.categoryId)
        startActivity(intent)

        Toast.makeText(context, "Add successful !!!", Toast.LENGTH_SHORT).show()
    }

    private fun setupNoteRecyclerView() {
        noteAdapter = ListNoteAdapter(object : OnItemClickListener {
            override fun onNoteClick(note: Note, isChoose: Boolean) {
                if (!isChoose) {
                    val intent = Intent(context, EditNoteActivity::class.java)
                    intent.putExtra("id", note.id)
                    intent.putExtra("title", note.title)
                    intent.putExtra("content", note.content)
                    intent.putExtra("categoryId", note.categoryId)
                    startActivity(intent)
                } else {
                    (activity as MainActivity).binding.topAppBar.setTitle("${noteAdapter.getSelectedItemsCount()}")
                    Log.d("TAG", "onNoteClick: ${noteAdapter.getSelectedItems()}")
                }
            }

            override fun onNoteLongClick(note: Note) {
                (activity as MainActivity).showDeleteIcon()
            }
        })

        binding.listNoteRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.listNoteRecyclerView.adapter = noteAdapter

        activity?.let {
            noteViewModel.getAllNote().observe(viewLifecycleOwner) { note ->
                noteAdapter.differ.submitList(note)
                updateUI(note)
            }
        }
    }

    private fun updateUI(note: List<Note>) {
        if (note.isNotEmpty()) {
            binding.listNoteRecyclerView.visibility = View.VISIBLE
        } else {
            binding.listNoteRecyclerView.visibility = View.GONE
        }
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


    fun showDeleteDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

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

    fun clearSelection() {
        noteAdapter.clearSelection()
    }

    fun showOptionDialog() {
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
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
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

    fun searchNote(query: String?) {
        Log.d("TAG", "searchNote: $query")
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
        menuInflater.inflate(R.menu.top_app_bar, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.search -> {
                val searchView = menuItem.actionView as SearchView
                searchView.setOnQueryTextListener(object : OnQueryTextListener {
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
                })
                true
            }

            else -> {
                false
            }
        }
    }
}

