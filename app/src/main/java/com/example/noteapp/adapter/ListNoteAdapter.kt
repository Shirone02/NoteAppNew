package com.example.noteapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
    private val context: Context,
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
    private lateinit var noteViewModel: NoteViewModel
    var isCreated = false

    inner class viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var time: TextView = itemView.findViewById(R.id.tvTime)
        var title: TextView = itemView.findViewById(R.id.tvTitle)
        var category: TextView = itemView.findViewById(R.id.tvCategory)
        var noteLayout: LinearLayout = itemView.findViewById(R.id.noteLayout)
    }

    private val selectedItems = mutableSetOf<Note>()
    var isChoosing = false
    var isInTrash = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListNoteAdapter.viewholder {
        categoryViewModel = (parent.context as MainActivity).categoryViewModel
        noteViewModel = (parent.context as MainActivity).noteViewModel

        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return viewholder(itemView)
    }

    @SuppressLint("SetTextI18n", "ResourceAsColor")
    override fun onBindViewHolder(holder: ListNoteAdapter.viewholder, position: Int) {
        if (differ.currentList[position].title == "") {
            holder.title.text = "Untitled"
        } else {
            holder.title.text = differ.currentList[position].title
        }

        if (!isCreated) {
            holder.time.text = "Last edit: " + differ.currentList[position].time
        } else {
            holder.time.text = "Created: " + differ.currentList[position].created
        }

        val categoryList = categoryViewModel.getCategoryNameById(differ.currentList[position].id)
        if (categoryList.size > 2) {
            holder.category.text =
                "${categoryList[0]}, ${categoryList[1]}, (+${categoryList.size - 2})"
        } else {
            if (categoryList.isNotEmpty()) {
                holder.category.text =
                    categoryList.toString().substring(1, categoryList.toString().length - 1)
            } else {
                holder.category.text = null
            }
        }

        if (differ.currentList[position].color != null) {
            val backgroundDrawable = GradientDrawable()
            backgroundDrawable.setColor(Color.parseColor(differ.currentList[position].color ?: "#FFFFFF"))
            backgroundDrawable.setStroke(5, R.color.brown)

            holder.noteLayout.background = backgroundDrawable
        } else {
            holder.noteLayout.setBackgroundResource(R.drawable.bg_item)
        }


        holder.itemView.isSelected = selectedItems.contains(differ.currentList[position])

        holder.itemView.setOnClickListener {
            if (holder.itemView.isSelected) {
                if (selectedItems.contains(differ.currentList[position])) {
                    selectedItems.remove(differ.currentList[position])

                }
                notifyItemChanged(position)  // Cập nhật lại item
            } else {
                if (isChoosing) {
                    selectedItems.add(differ.currentList[position])
                    notifyDataSetChanged()
                } else {
                    if (isInTrash) {
                        showActionDialog(context, position)
                    }
                }
            }

            onItemClickListener.onNoteClick(
                differ.currentList[position],
                holder.itemView.isSelected
            )
        }

        holder.itemView.setOnLongClickListener {
            isChoosing = true
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

    //hien thi hop thoai action
    private fun showActionDialog(context: Context, position: Int) {
        // Tạo layout inflater và inflate layout cho hộp thoại
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_action_in_trash, null)
        val title = dialogView.findViewById<TextView>(R.id.trashTitle)
        val content = dialogView.findViewById<TextView>(R.id.trashContent)
        val actionRadioGroup = dialogView.findViewById<RadioGroup>(R.id.actionRadioGroup)

        title.text = differ.currentList[position].title
        content.text = differ.currentList[position].content

        val alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("OK") { dialog, _ ->
                val selectedAction = when (actionRadioGroup.checkedRadioButtonId) {
                    R.id.rbUndelete -> "Undelete"
                    R.id.rbDelete -> "Delete"
                    else -> null
                }
                handleNoteAction(differ.currentList[position], selectedAction)
                dialog.dismiss()
            }.create()

        alertDialog.show()
    }

    private fun handleNoteAction(note: Note, selectedAction: String?) {
        when (selectedAction) {
            "Undelete" -> {
                val noteIds: List<Int> = listOf(note.id)
                noteViewModel.restoreFromTrash(noteIds)
            }

            "Delete" -> {
                val builder = AlertDialog.Builder(context)
                    .setMessage("The note will be deleted permanently! Are you sure that you want to delete the '${note.title}' note?")
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }.setPositiveButton("Yes") { dialog, _ ->
                        noteViewModel.deleteNote(note)
                    }
                builder.create().show()
            }
        }
    }

}