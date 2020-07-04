package com.intkhabahmed.smartnotes.adapters.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.intkhabahmed.smartnotes.models.Note

abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(note: Note?)
}