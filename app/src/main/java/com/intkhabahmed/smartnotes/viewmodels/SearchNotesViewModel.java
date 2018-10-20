package com.intkhabahmed.smartnotes.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.models.Note;

import java.util.List;

public class SearchNotesViewModel extends ViewModel {
    private LiveData<List<Note>> notes;

    SearchNotesViewModel(String title, int trashed) {
        if (notes == null) {
            setNotes(title, trashed);
        }
    }

    public void setNotes(String title, int trashed) {
        notes = NoteRepository.getInstance().getNotesByTitleAndAvailability(title, trashed);
    }

    public LiveData<List<Note>> getNotes() {
        return notes;
    }
}
