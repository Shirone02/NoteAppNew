package com.example.noteapp.adapter

import android.annotation.SuppressLint
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
    private val onItemClickListener: OnItemClickListener
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
        var content: TextView = itemView.findViewById(R.id.tvContent)
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
        holder.title.text = differ.currentList[position].title

        if (differ.currentList[position].content.isEmpty()) {
            holder.content.visibility = View.GONE
        } else {
            holder.content.text = differ.currentList[position].content
        }

        holder.time.text = "Last edit: " + differ.currentList[position].time

        holder.category.text = categoryViewModel.getCategoryNameById(differ.currentList[position].id)

        holder.itemView.isSelected = selectedItems.contains(differ.currentList[position])

        holder.itemView.setOnClickListener {
            onItemClickListener.onNoteClick(
                differ.currentList[position],
                holder.itemView.isSelected
            )

            if (holder.itemView.isSelected) {
                if (selectedItems.contains(differ.currentList[position])) {
                    selectedItems.remove(differ.currentList[position])
                    holder.itemView.isSelected = false
                }
                notifyItemChanged(position)  // Cập nhật lại item
            }
        }

        holder.itemView.setOnLongClickListener {
            onItemClickListener.onNoteLongClick(differ.currentList[position])
            toggleSelection(differ.currentList[position])
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

    fun getSelectedItemsCount(): Int{
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