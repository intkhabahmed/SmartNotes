package com.intkhabahmed.smartnotes.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.models.Note;

import java.util.List;

public class NotesViewModel extends ViewModel {
    private LiveData<List<Note>> notes;

    NotesViewModel(String noteType, int trashed) {
        if (notes == null) {
            setNotes(noteType, trashed);
        }
    }

    private void setNotes(String noteType, int trashed) {
        notes = NoteRepository.getInstance().getNotesByTypeAndAvailability(noteType, trashed);
    }

    public LiveData<List<Note>> getNotes() {
        return notes;
    }
}
