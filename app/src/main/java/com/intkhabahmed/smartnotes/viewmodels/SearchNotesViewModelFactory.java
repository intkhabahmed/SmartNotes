package com.intkhabahmed.smartnotes.viewmodels;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class SearchNotesViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private String title;
    private int trashed;

    public SearchNotesViewModelFactory(String title, int trashed) {
        this.title = title;
        this.trashed = trashed;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new SearchNotesViewModel(title, trashed);
    }
}
