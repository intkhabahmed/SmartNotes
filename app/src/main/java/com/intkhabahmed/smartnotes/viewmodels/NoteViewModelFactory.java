package com.intkhabahmed.smartnotes.viewmodels;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class NoteViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private int noteId;

    public NoteViewModelFactory(int noteId) {
        this.noteId = noteId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new NoteViewModel(noteId);
    }
}
