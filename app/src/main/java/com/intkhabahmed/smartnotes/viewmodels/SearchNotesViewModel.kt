package com.intkhabahmed.smartnotes.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList

import com.intkhabahmed.smartnotes.database.NoteRepository
import com.intkhabahmed.smartnotes.models.Note

class SearchNotesViewModel internal constructor(title: String, trashed: Int) : ViewModel() {
    lateinit var notes: LiveData<PagedList<Note>>

    init {
        setNotes(title, trashed)
    }

    fun setNotes(title: String, trashed: Int) {
        notes = LivePagedListBuilder(NoteRepository.getInstance().getNotesByTitleAndAvailability(title, trashed),
                PagedList.Config.Builder().setPageSize(10).setInitialLoadSizeHint(20).setEnablePlaceholders(true).build()).build()
    }
}
