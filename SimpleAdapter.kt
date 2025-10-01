package com.example.mymusicapp

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SimpleAdapter(
    private val tracks: List<Pair<String, Uri>>,
    private val onClick: (String, Uri) -> Unit
) : RecyclerView.Adapter<SimpleAdapter.ViewHolder>() {

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (title, uri) = tracks[position]
        holder.title.text = title
        holder.itemView.setOnClickListener { onClick(title, uri) }
    }

    override fun getItemCount() = tracks.size
}
