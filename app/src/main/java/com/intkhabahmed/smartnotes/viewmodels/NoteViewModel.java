package com.intkhabahmed.smartnotes.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.models.Note;

public class NoteViewModel extends ViewModel {
    private LiveData<Note> note;

    NoteViewModel(int noteId) {
        setNote(noteId);
    }

    private void setNote(int noteId) {
        note = NoteRepository.getInstance().getNoteById(noteId);
    }

    public LiveData<Note> getNote() {
        return note;
    }
}
