package com.intkhabahmed.smartnotes.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intkhabahmed.smartnotes.R
import com.intkhabahmed.smartnotes.adapters.view.BaseViewHolder
import com.intkhabahmed.smartnotes.models.ChecklistItem
import com.intkhabahmed.smartnotes.models.Note
import com.intkhabahmed.smartnotes.utils.AppConstants
import com.intkhabahmed.smartnotes.utils.NoteUtils
import kotlinx.android.synthetic.main.image_note_item.view.note_image_view
import kotlinx.android.synthetic.main.image_note_item.view.note_menu_button
import kotlinx.android.synthetic.main.image_note_item.view.tv_note_date_created
import kotlinx.android.synthetic.main.image_note_item.view.tv_note_title
import kotlinx.android.synthetic.main.note_item.view.item_menu_button
import kotlinx.android.synthetic.main.note_item.view.note_create_date
import kotlinx.android.synthetic.main.note_item.view.note_description
import kotlinx.android.synthetic.main.note_item.view.note_title
import java.io.File

/**
 * Created by INTKHAB on 23-03-2018.
 */

class NotesAdapter(private val mOnItemClickListener: OnItemClickListener) : PagedListAdapter<Note, BaseViewHolder>(DIFF_CALLBACK) {

    interface OnItemClickListener {
        fun onMenuItemClick(view: View, note: Note)

        fun onItemClick(noteId: Int, noteType: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view: View
        return when (viewType) {
            R.layout.note_item -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
                TextNotesViewHolder(view)
            }
            R.layout.image_note_item -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.image_note_item, parent, false)
                ImageNotesViewHolder(view)
            }
            else -> throw IllegalArgumentException(AppConstants.INVALID_VIEW_TYPE)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        val note: Note? = getItem(position)
        return when (note?.noteType) {
            AppConstants.SIMPLE_NOTE,
            AppConstants.CHECKLIST -> R.layout.note_item
            else -> R.layout.image_note_item
        }
    }

    internal inner class TextNotesViewHolder(itemView: View) : BaseViewHolder(itemView), View.OnClickListener {

        init {
            itemView.note_title.setOnClickListener(this)
            itemView.note_description.setOnClickListener(this)
            itemView.note_create_date.setOnClickListener(this)
            itemView.item_menu_button.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val note = getItem(adapterPosition)!!
            if (view is ImageButton) {
                mOnItemClickListener.onMenuItemClick(view, note)
            } else if (view is TextView) {
                mOnItemClickListener.onItemClick(note.noteId, note.noteType ?: "")
            }
        }

        override fun bind(note: Note?) {
            note?.let {
                if (note.noteType == AppConstants.SIMPLE_NOTE) {
                    itemView.note_description.text = note.description
                } else if (note.noteType == AppConstants.CHECKLIST) {
                    val checklistItems = Gson().fromJson<List<ChecklistItem>>(note.description, object : TypeToken<List<ChecklistItem>>() {
                    }.type)

                    val noOfItems = if (checklistItems.size >= 2) 2 else checklistItems.size
                    itemView.note_description.text = ""
                    for (i in 0 until noOfItems) {
                        itemView.note_description
                            .append(checklistItems!![i].title)
                        if (checklistItems[i].isChecked) {
                            itemView.note_description.append(" " + itemView.context.getString(R.string.checkmark_unicode))
                        }
                        if (i < noOfItems - 1) {
                            itemView.note_description.append("\n")
                        }
                    }
                    if (checklistItems != null && checklistItems.size > 2) {
                        itemView.note_description.append(" ...")
                    }
                }
                itemView.note_title.text = note.noteTitle
                itemView.note_create_date.text = NoteUtils.getFormattedTime(note.dateCreated,
                    System.currentTimeMillis())
            }
        }
    }

    internal inner class ImageNotesViewHolder(itemView: View) : BaseViewHolder(itemView), View.OnClickListener {

        init {
            itemView.tv_note_title.setOnClickListener(this)
            itemView.note_image_view.setOnClickListener(this)
            itemView.tv_note_date_created.setOnClickListener(this)
            itemView.note_menu_button.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val note = getItem(adapterPosition)!!
            if (view is ImageButton) {
                mOnItemClickListener.onMenuItemClick(view, note)
            } else if (view is TextView || view is ImageView) {
                mOnItemClickListener.onItemClick(note.noteId, note.noteType ?: "")
            }
        }

        override fun bind(note: Note?) {
            note?.let {
                val imageFile = File(note.description!!)
                if (imageFile.exists()) {
                    Glide.with(itemView.context).asDrawable().load(Uri.fromFile(imageFile)).into(itemView.note_image_view)
                }
                itemView.tv_note_title.text = note.noteTitle
                itemView.tv_note_date_created.text = NoteUtils.getFormattedTime(note.dateCreated,
                    System.currentTimeMillis())
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
