package com.intkhabahmed.smartnotes.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class NoteViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private int noteId;

    public NoteViewModelFactory(int noteId) {
        this.noteId = noteId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(NoteViewModel.class)) {
            return (T) new NoteViewModel(noteId);
        }
        throw new ClassCastException("Unknown ViewModel Class");
    }
}
