package com.example.noteapp.fragments

import android.app.AlertDialog
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
import androidx.core.view.MenuProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.R
import com.example.noteapp.activities.EditNoteActivity
import com.example.noteapp.activities.MainActivity
import com.example.noteapp.adapter.ListCategoryAdapter
import com.example.noteapp.adapter.ListNoteAdapter
import com.example.noteapp.databinding.FragmentUncategorizedBinding
import com.example.noteapp.listeners.OnItemClickListener
import com.example.noteapp.models.Category
import com.example.noteapp.models.Note
import com.example.noteapp.viewmodel.CategoryViewModel
import com.example.noteapp.viewmodel.NoteViewModel

class UncategorizedFragment : Fragment(R.layout.fragment_uncategorized) {

    private val binding: FragmentUncategorizedBinding by lazy {
        FragmentUncategorizedBinding.inflate(layoutInflater)
    }

    private lateinit var noteViewModel: NoteViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var noteAdapter: ListNoteAdapter
    private lateinit var categoryAdapter: ListCategoryAdapter
    private lateinit var uncategorizedView: View
    private lateinit var categories: List<Category>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteViewModel = (activity as MainActivity).noteViewModel
        categoryViewModel = (activity as MainActivity).categoryViewModel
        uncategorizedView = view

        setUpNoteRecyclerView()

    }

    fun showCategorizeDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("Select category")
            .setPositiveButton("OK") { dialog, which ->
                val selectedCategories = mutableListOf<Category>()
                for(i in categories.indices){
                    if(BooleanArray(categories.size)[i]){
                        selectedCategories.add(categories[i])
                    }
                }
                Log.d("TAG", "showCategorizeDialog: $selectedCategories")
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .setMultiChoiceItems(categories.map { it.categoryName }.toTypedArray(), BooleanArray(categories.size)){ dialog, which, isChecked ->
                BooleanArray(categories.size)[which] = isChecked
            }
        builder.create().show()
    }

    private fun setUpNoteRecyclerView() {
        noteAdapter = ListNoteAdapter(object : OnItemClickListener {
            override fun onNoteClick(note: Note, isChoose: Boolean) {
                if (!isChoose) {
                    val intent = Intent(activity, EditNoteActivity::class.java)
                    intent.putExtra("id", note.id)
                    intent.putExtra("title", note.title)
                    intent.putExtra("content", note.content)
                    startActivity(intent)
                }
            }

            override fun onNoteLongClick(note: Note) {

            }
        })

        categoryAdapter = ListCategoryAdapter(requireContext())

        binding.uncategorizedNote.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.uncategorizedNote.adapter = noteAdapter

        activity?.let {
            noteViewModel.getNotesByCategory(null).observe(viewLifecycleOwner) { note ->
                noteAdapter.differ.submitList(note)
                updateUI(note)
            }
        }

        activity?.let {
            categoryViewModel.getAllCategory().observe(viewLifecycleOwner){category ->
                categoryAdapter.differ.submitList(category)
                categories = categoryAdapter.differ.currentList
            }
        }
    }

    private fun updateUI(note: List<Note>) {
        if (note.isNotEmpty()) {
            binding.uncategorizedNote.visibility = View.VISIBLE
        } else {
            binding.uncategorizedNote.visibility = View.GONE
        }
    }

}