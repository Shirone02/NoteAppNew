package com.example.noteapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.activities.MainActivity
import com.example.noteapp.adapter.ListCategoryAdapter
import com.example.noteapp.databinding.FragmentEditCategoriesBinding
import com.example.noteapp.models.Category
import com.example.noteapp.viewmodel.CategoryViewModel

class EditCategoriesFragment : Fragment() {

    private val binding: FragmentEditCategoriesBinding by lazy {
        FragmentEditCategoriesBinding.inflate(layoutInflater)
    }

    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var categoryAdapter: ListCategoryAdapter
    private lateinit var editView: View

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
        binding.addBtn.setOnClickListener { addCategory() }
    }

    // thêm category
    private fun addCategory(){
        val newCategory = Category(0, binding.newCateName.text.toString())
        categoryViewModel.addCategory(newCategory)

        Toast.makeText(context, "Add successful !!!", Toast.LENGTH_SHORT).show()
    }

    private fun setupCategoryRecyclerView() {
        categoryAdapter = ListCategoryAdapter(requireContext())
        binding.categoryRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.categoryRecyclerView.adapter = categoryAdapter

        activity?.let {
            categoryViewModel.getAllCategory().observe(viewLifecycleOwner){category ->
                categoryAdapter.differ.submitList(category)
                updateUI(category)
            }
        }
    }

    private fun updateUI(category: List<Category>){
        if(category.isNotEmpty()){
            binding.categoryRecyclerView.visibility = View.VISIBLE
        } else {
            binding.categoryRecyclerView.visibility = View.GONE
        }
    }
}