package com.intkhabahmed.smartnotes.database;

import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.utils.Global;

import java.util.List;

public class NoteRepository {
    private static NoteRepository sInstance;
    private static final Object LOCK = new Object();

    public static NoteRepository getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new NoteRepository();
            }
        }
        return sInstance;
    }

    public List<Note> getNotesByTypeAndAvailability(String type, int trashed, String sortBy) {
        return Global.getDbInstance().notesDao().getNotesByTypeAndAvailability(type, trashed, sortBy);
    }

    public List<Note> getNotesByTitleAndAvailability(String title, int trashed) {
        return Global.getDbInstance().notesDao().getNotesByTitleAndAvailability(title, trashed);
    }

    public long insertNote(Note note) {
        return Global.getDbInstance().notesDao().insertNote(note);
    }

    public int updateNote (Note note) {
        return Global.getDbInstance().notesDao().updateNote(note);
    }

    public int deleteNote(Note note) {
        return Global.getDbInstance().notesDao().deleteNote(note);
    }

    public int moveNoteToTrash(Note note) {
        note.setTrashed(1);
        return Global.getDbInstance().notesDao().updateNote(note);
    }

    public int recoverNoteFromTrash(Note note) {
        note.setTrashed(0);
        return Global.getDbInstance().notesDao().updateNote(note);
    }
}
