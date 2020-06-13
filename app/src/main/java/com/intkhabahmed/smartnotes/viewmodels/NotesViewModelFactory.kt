package com.intkhabahmed.smartnotes.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory

class NotesViewModelFactory(private val noteType: String, private val trashed: Int) : NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            return NotesViewModel(noteType, trashed) as T
        }
        throw ClassCastException("Unknown ViewModel Class")
    }

}