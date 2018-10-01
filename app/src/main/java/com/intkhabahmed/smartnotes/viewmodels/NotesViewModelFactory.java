package com.intkhabahmed.smartnotes.viewmodels;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

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
        return (T) new NotesViewModel(noteType, trashed);
    }
}
