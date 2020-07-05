package com.intkhabahmed.smartnotes.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.intkhabahmed.smartnotes.database.NoteRepository
import com.intkhabahmed.smartnotes.models.Note

class NoteViewModel internal constructor(noteId: Int) : ViewModel() {
    var note: LiveData<Note>? = null
        private set

    private fun setNote(noteId: Int) {
        note = NoteRepository.instance?.getNoteById(noteId)
    }

    init {
        setNote(noteId)
    }
}