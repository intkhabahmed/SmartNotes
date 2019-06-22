package com.intkhabahmed.smartnotes.adapters

import android.arch.paging.PagedListAdapter
import android.content.Context
import android.net.Uri
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intkhabahmed.smartnotes.R
import com.intkhabahmed.smartnotes.models.ChecklistItem
import com.intkhabahmed.smartnotes.models.Note
import com.intkhabahmed.smartnotes.utils.AppConstants
import com.intkhabahmed.smartnotes.utils.NoteUtils
import java.io.File

/**
 * Created by INTKHAB on 23-03-2018.
 */

class NotesAdapter(private val mContext: Context, private val mOnItemClickListener: OnItemClickListener) : PagedListAdapter<Note, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    interface OnItemClickListener {
        fun onMenuItemClick(view: View, note: Note)

        fun onItemClick(noteId: Int, noteType: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {
            0 -> {
                view = LayoutInflater.from(mContext).inflate(R.layout.note_item, parent, false)
                TextNotesViewHolder(view)
            }
            1 -> {
                view = LayoutInflater.from(mContext).inflate(R.layout.image_note_item, parent, false)
                ImageNotesViewHolder(view)
            }
            else -> throw IllegalArgumentException(AppConstants.INVALID_VIEW_TYPE)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val note = getItem(holder.adapterPosition)!!
        when (holder.itemViewType) {
            0 -> {
                val textNotesViewHolder = holder as TextNotesViewHolder
                if (note.noteType == mContext.getString(R.string.simple_note)) {
                    textNotesViewHolder.noteDescriptionTextView.text = note.description
                } else if (note.noteType == mContext.getString(R.string.checklist)) {
                    val checklistItems = Gson().fromJson<List<ChecklistItem>>(note.description, object : TypeToken<List<ChecklistItem>>() {
                    }.type)

                    val noOfItems = if (checklistItems.size >= 2) 2 else checklistItems.size
                    textNotesViewHolder.noteDescriptionTextView.text = ""
                    for (i in 0 until noOfItems) {
                        textNotesViewHolder.noteDescriptionTextView
                                .append(checklistItems!![i].title.toString())
                        if (checklistItems[i].isChecked) {
                            textNotesViewHolder.noteDescriptionTextView.append(" " + mContext.getString(R.string.checkmark_unicode))
                        }
                        if (i < noOfItems - 1) {
                            textNotesViewHolder.noteDescriptionTextView.append("\n")
                        }
                    }
                    if (checklistItems != null && checklistItems.size > 2) {
                        textNotesViewHolder.noteDescriptionTextView.append(" ...")
                    }
                }
                textNotesViewHolder.noteTitleTextView.text = note.noteTitle
                textNotesViewHolder.noteCreateDateTextView.text = NoteUtils.getFormattedTime(note.dateCreated,
                        System.currentTimeMillis())
            }
            1 -> {
                val imageNotesViewHolder = holder as ImageNotesViewHolder
                val imageFile = File(note.description)
                if (imageFile.exists()) {
                    Glide.with(mContext).asDrawable().load(Uri.fromFile(imageFile)).into(imageNotesViewHolder.noteImageView)
                }
                imageNotesViewHolder.noteTitleTextView.text = note.noteTitle
                imageNotesViewHolder.noteCreateDateTextView.text = NoteUtils.getFormattedTime(note.dateCreated,
                        System.currentTimeMillis())
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val note: Note? = getItem(position)
        return if (note?.noteType == mContext.getString(R.string.simple_note) || note?.noteType == mContext.getString(R.string.checklist)) {
            0
        } else 1
    }

    internal inner class TextNotesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var noteTitleTextView: TextView = itemView.findViewById(R.id.note_title)
        var noteDescriptionTextView: TextView = itemView.findViewById(R.id.note_description)
        var noteCreateDateTextView: TextView = itemView.findViewById(R.id.note_create_date)
        var itemMenuButton: ImageButton = itemView.findViewById(R.id.item_menu_button)

        init {
            noteTitleTextView.setOnClickListener(this)
            noteDescriptionTextView.setOnClickListener(this)
            noteCreateDateTextView.setOnClickListener(this)
            itemMenuButton.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val note = getItem(adapterPosition)!!
            if (view is ImageButton) {
                mOnItemClickListener.onMenuItemClick(view, note)
            } else if (view is TextView) {
                mOnItemClickListener.onItemClick(note.noteId, note.noteType ?: "")
            }
        }
    }

    internal inner class ImageNotesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var noteTitleTextView: TextView = itemView.findViewById(R.id.tv_note_title)
        var noteImageView: ImageView = itemView.findViewById(R.id.note_image_view)
        var noteCreateDateTextView: TextView = itemView.findViewById(R.id.tv_note_date_created)
        var itemMenuButton: ImageButton = itemView.findViewById(R.id.note_menu_button)

        init {
            noteTitleTextView.setOnClickListener(this)
            noteImageView.setOnClickListener(this)
            noteCreateDateTextView.setOnClickListener(this)
            itemMenuButton.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val note = getItem(adapterPosition)!!
            if (view is ImageButton) {
                mOnItemClickListener.onMenuItemClick(view, note)
            } else if (view is TextView || view is ImageView) {
                mOnItemClickListener.onItemClick(note.noteId, note.noteType ?: "")
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(old: Note, new: Note): Boolean {
                return old.noteId == new.noteId
            }

            override fun areContentsTheSame(old: Note, new: Note): Boolean {
                return old == new
            }
        }
    }
}
