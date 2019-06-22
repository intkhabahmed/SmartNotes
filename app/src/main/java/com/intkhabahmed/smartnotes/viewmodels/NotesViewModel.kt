package com.intkhabahmed.smartnotes.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList

import com.intkhabahmed.smartnotes.database.NoteRepository
import com.intkhabahmed.smartnotes.models.Note

class NotesViewModel internal constructor(noteType: String, trashed: Int) : ViewModel() {
    lateinit var notes: LiveData<PagedList<Note>>

    init {
        setNotes(noteType, trashed)
    }

    fun setNotes(noteType: String, trashed: Int) {
        notes = LivePagedListBuilder(
                NoteRepository.getInstance().getNotesByTypeAndAvailability(noteType, trashed),
                PagedList.Config.Builder().setPageSize(10).setInitialLoadSizeHint(20).setEnablePlaceholders(true).build()
        ).build()
    }
}
