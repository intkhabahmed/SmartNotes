package com.intkhabahmed.smartnotes.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class NotesViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private String noteType;
    private int trashed;

    public NotesViewModelFactory(String noteType, int trashed) {
        this.noteType = noteType;
        this.trashed = trashed;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(NoteViewModel.class)) {
            return (T) new NotesViewModel(noteType, trashed);
        }
        throw new ClassCastException("Unknown ViewModel Class");
    }
}
