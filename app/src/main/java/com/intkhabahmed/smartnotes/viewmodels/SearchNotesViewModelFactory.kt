package com.intkhabahmed.smartnotes.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory

class SearchNotesViewModelFactory(private val title: String?, private val trashed: Int) : NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            return SearchNotesViewModel(title, trashed) as T
        }
        throw ClassCastException("Unknown ViewModel Class")
    }

}