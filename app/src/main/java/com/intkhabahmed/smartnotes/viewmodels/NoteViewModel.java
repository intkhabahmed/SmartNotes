package com.intkhabahmed.smartnotes.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.models.Note;

public class NoteViewModel extends ViewModel {
    private LiveData<Note> note;

    NoteViewModel(int noteId) {
        if (note == null) {
            setNote(noteId);
        }
    }

    private void setNote(int noteId) {
        note = NoteRepository.getInstance().getNoteById(noteId);
    }

    public LiveData<Note> getNote() {
        return note;
    }
}
