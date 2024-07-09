package com.example.noteapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.activities.MainActivity
import com.example.noteapp.listeners.OnItemClickListener
import com.example.noteapp.models.Note
import com.example.noteapp.viewmodel.CategoryViewModel
import com.example.noteapp.viewmodel.NoteViewModel

class ListNoteAdapter(
    private val onItemClickListener: OnItemClickListener,
) : RecyclerView.Adapter<ListNoteAdapter.viewholder>() {

    private val differCallBack = object : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id && oldItem.content == newItem.content && oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)

    private lateinit var categoryViewModel: CategoryViewModel

    inner class viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var time: TextView = itemView.findViewById(R.id.tvTime)
        var title: TextView = itemView.findViewById(R.id.tvTitle)
        var category: TextView = itemView.findViewById(R.id.tvCategory)
    }

    private val selectedItems = mutableSetOf<Note>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListNoteAdapter.viewholder {
        categoryViewModel = (parent.context as MainActivity).categoryViewModel

        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return viewholder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ListNoteAdapter.viewholder, position: Int) {
        if (differ.currentList[position].title == "") {
            holder.title.text = "Untitled"
        } else {
            holder.title.text = differ.currentList[position].title
        }

        holder.time.text = "Last edit: " + differ.currentList[position].time

        val categoryList = categoryViewModel.getCategoryNameById(differ.currentList[position].id)
        if (categoryList.size > 2) {
            holder.category.text =
                "${categoryList[0]}, ${categoryList[1]}, (+${categoryList.size - 2})"
        } else {
            if (categoryList.isNotEmpty()) {
                holder.category.text = "$categoryList"
            } else {
                holder.category.text = null
            }

        }

        holder.itemView.isSelected = selectedItems.contains(differ.currentList[position])

        holder.itemView.setOnClickListener {
            if (holder.itemView.isSelected) {
                if (selectedItems.contains(differ.currentList[position])) {
                    selectedItems.remove(differ.currentList[position])
                    //holder.itemView.isSelected = false
                }
                notifyItemChanged(position)  // Cập nhật lại item
            } else {
                selectedItems.add(differ.currentList[position])
                notifyDataSetChanged()
            }

            onItemClickListener.onNoteClick(
                differ.currentList[position],
                holder.itemView.isSelected
            )
        }

        holder.itemView.setOnLongClickListener {
            toggleSelection(differ.currentList[position])
            onItemClickListener.onNoteLongClick(differ.currentList[position])
            notifyItemChanged(position)
            true
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun removeSelectedItems() {
        val newList = differ.currentList.toMutableList().apply {
            removeAll(selectedItems)
        }
        differ.submitList(newList)
        selectedItems.clear()
    }

    fun getSelectedItemsCount(): Int {
        return selectedItems.size
    }

    fun getSelectedItems(): Set<Note> {
        return selectedItems
    }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun selectAllItem() {
        if (selectedItems.isEmpty()) {
            selectedItems.addAll(differ.currentList)
        } else {
            if (selectedItems.size < differ.currentList.size) {
                selectedItems.clear()
                selectedItems.addAll(differ.currentList)
            } else {
                selectedItems.clear()
            }
        }
        notifyDataSetChanged()
    }

    private fun toggleSelection(note: Note) {
        if (selectedItems.contains(note)) {
            selectedItems.remove(note)
        } else {
            selectedItems.add(note)
        }
        notifyDataSetChanged()
    }

}