package com.example.noteapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.listeners.OnColorClickListener

class ListColorAdapter(
    private val listColor: List<String>,
    private val onColorClickListener: OnColorClickListener
) : RecyclerView.Adapter<ListColorAdapter.viewholder>() {

    private var selectedPosition = -1

    inner class viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgColor: View = itemView.findViewById(R.id.imgColor)
        val checkmark: ImageView = itemView.findViewById(R.id.checkmark_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_color, parent, false)
        return viewholder(itemView)
    }

    override fun getItemCount(): Int {
        return listColor.size
    }

    override fun onBindViewHolder(holder: viewholder, position: Int) {
        holder.imgColor.setBackgroundColor(Color.parseColor(listColor[position]))
        holder.checkmark.visibility = if (position == selectedPosition) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            getSelectedPosition(position)
            notifyDataSetChanged()
            onColorClickListener.onColorClick(listColor[position])
        }
    }

    private fun getSelectedPosition(position: Int) {
        selectedPosition = position
    }
}