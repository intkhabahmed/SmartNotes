package com.intkhabahmed.smartnotes.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory

class NoteViewModelFactory(private val noteId: Int) : NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            return NoteViewModel(noteId) as T
        }
        throw ClassCastException("Unknown ViewModel Class")
    }

}