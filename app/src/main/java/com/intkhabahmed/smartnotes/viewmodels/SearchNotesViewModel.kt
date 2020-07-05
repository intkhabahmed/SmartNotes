package com.intkhabahmed.smartnotes.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList

import com.intkhabahmed.smartnotes.database.NoteRepository
import com.intkhabahmed.smartnotes.models.Note

class SearchNotesViewModel internal constructor(title: String?, trashed: Int) : ViewModel() {
    lateinit var notes: LiveData<PagedList<Note>>

    init {
        setNotes(title, trashed)
    }

    fun setNotes(title: String?, trashed: Int) {
        notes = LivePagedListBuilder(NoteRepository.instance!!.getNotesByTitleAndAvailability(title, trashed),
                PagedList.Config.Builder().setPageSize(10).setInitialLoadSizeHint(20).setEnablePlaceholders(true).build()).build()
    }
}
