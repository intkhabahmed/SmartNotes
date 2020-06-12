package com.intkhabahmed.smartnotes.viewmodels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class SearchNotesViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private String title;
    private int trashed;

    public SearchNotesViewModelFactory(@Nullable String title, int trashed) {
        this.title = title;
        this.trashed = trashed;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new SearchNotesViewModel(title, trashed);
    }
}
