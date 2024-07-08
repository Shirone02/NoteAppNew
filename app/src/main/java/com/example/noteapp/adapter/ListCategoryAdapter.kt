package com.example.noteapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.activities.MainActivity
import com.example.noteapp.models.Category
import com.example.noteapp.viewmodel.CategoryViewModel

class ListCategoryAdapter(private val context: Context) :
    RecyclerView.Adapter<ListCategoryAdapter.viewholder>() {

    inner class viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var categoryName: TextView = itemView.findViewById(R.id.categoryName)
        var editBtn: ImageView = itemView.findViewById(R.id.editBtn)
        var deleteBtn: ImageView = itemView.findViewById(R.id.deleteBtn)

    }

    private lateinit var categoryViewModel: CategoryViewModel

    private val differCallBack = object: DiffUtil.ItemCallback<Category>(){
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.categoryName == newItem.categoryName
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListCategoryAdapter.viewholder {
        categoryViewModel = (context as MainActivity).categoryViewModel

        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return viewholder(itemView)
    }

    override fun onBindViewHolder(holder: ListCategoryAdapter.viewholder, position: Int) {
        holder.categoryName.text = differ.currentList[position].categoryName

        holder.editBtn.setOnClickListener {
            showEditCategoryDialog(context, position)
        }

        holder.deleteBtn.setOnClickListener {
           showDeleteCategoryDialog(context, position)
        }
    }

    private fun showDeleteCategoryDialog(context: Context, position: Int){
        val alertDialog = AlertDialog.Builder(context)
            .setMessage("Delete category '${differ.currentList[position].categoryName}'? Notes from the category won't be deleted.")
            .setNegativeButton("Cancel"){dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("OK"){dialog, _ ->
                categoryViewModel.deleteCategory(differ.currentList[position])
                notifyDataSetChanged()
                dialog.dismiss()
            }.create()
        alertDialog.show()
    }

    private fun showEditCategoryDialog(context: Context, position: Int) {
        // Tạo layout inflater và inflate layout cho hộp thoại
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_category, null)
        val editText = dialogView.findViewById<EditText>(R.id.editText)
        editText.setText(differ.currentList[position].categoryName)

        val alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setNegativeButton("Cancel"){dialog,_ ->
                dialog.dismiss()
            }

            .setPositiveButton("OK"){ dialog, _ ->
                val inputText = editText.text.toString()

                val newCategory = Category(differ.currentList[position].id, inputText)
                categoryViewModel.updateCategory(newCategory)
                notifyDataSetChanged()
                dialog.dismiss()
            }.create()

        alertDialog.show()
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}