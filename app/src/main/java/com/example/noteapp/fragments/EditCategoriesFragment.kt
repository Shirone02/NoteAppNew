package com.example.noteapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.R
import com.example.noteapp.activities.MainActivity
import com.example.noteapp.adapter.ListCategoryAdapter
import com.example.noteapp.databinding.FragmentEditCategoriesBinding
import com.example.noteapp.models.Category
import com.example.noteapp.models.Note
import com.example.noteapp.viewmodel.CategoryViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditCategoriesFragment : Fragment() {

    private val binding: FragmentEditCategoriesBinding by lazy {
        FragmentEditCategoriesBinding.inflate(layoutInflater)
    }

    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var categoryAdapter: ListCategoryAdapter
    private lateinit var editView: View

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var newCategoryId: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryViewModel = (activity as MainActivity).categoryViewModel
        editView = view

        setupCategoryRecyclerView()
        binding.addBtn.setOnClickListener {
            addCategory()
        }

        (activity as MainActivity).let { mainActivity ->
            val toolbar = mainActivity.findViewById<Toolbar>(R.id.topAppBar)
            toolbar.setTitle("Categories")
        }
    }

    // thêm category
    private fun addCategory() {
        val newCategory = Category(0, binding.newCateName.text.toString())
        val user = mAuth.currentUser
        val myRef = database.getReference("Category").child(user!!.uid).child(newCategory.id.toString())

        myRef.setValue(newCategory)
       // categoryViewModel.addCategory(newCategory)
        binding.newCateName.text = null

        Toast.makeText(context, "Add successful !!!", Toast.LENGTH_SHORT).show()
    }

    private fun setupCategoryRecyclerView() {
        categoryAdapter = ListCategoryAdapter(requireContext())
        binding.categoryRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.categoryRecyclerView.adapter = categoryAdapter

//        activity?.let {
//            categoryViewModel.getAllCategory().observe(viewLifecycleOwner) { category ->
//                categoryAdapter.differ.submitList(category)
//                updateUI(category)
//            }
//        }

        updateListCategories()
    }

    private fun updateListCategories() {
        val myRef = database.getReference("Category").child(mAuth.currentUser!!.uid)
        val list = ArrayList<Category>()
        myRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                if(snapshot.exists()){
                    for(issue in snapshot.children){
                        list.add(issue.getValue(Category::class.java)!!)
                    }

                    if (list.isNotEmpty()) {
                        binding.categoryRecyclerView.layoutManager = GridLayoutManager(context, 1)
                        categoryAdapter.differ.submitList(list)
                        binding.categoryRecyclerView.adapter = categoryAdapter
                        newCategoryId += 1
                    }
                    else {
                        newCategoryId = 0
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun updateUI(category: List<Category>) {
        if (category.isNotEmpty()) {
            binding.categoryRecyclerView.visibility = View.VISIBLE
        } else {
            binding.categoryRecyclerView.visibility = View.GONE
        }
    }
}