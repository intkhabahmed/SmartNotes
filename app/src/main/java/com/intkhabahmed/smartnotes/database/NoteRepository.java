package com.intkhabahmed.smartnotes.database;

import android.arch.lifecycle.LiveData;

import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.utils.AppExecutors;
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

    public LiveData<List<Note>> getNotesByTypeAndAvailability(String type, int trashed) {
        return Global.getDbInstance().notesDao().getNotesByTypeAndAvailability(type, trashed,
                Global.getSortCriteria());
    }

    public LiveData<List<Note>> getNotesByTitleAndAvailability(String title, int trashed) {
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

    public void moveNoteToTrash(final Note note) {
        note.setTrashed(1);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Global.getDbInstance().notesDao().updateNote(note);
            }
        });
    }

    public void recoverNoteFromTrash(final Note note) {
        note.setTrashed(0);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Global.getDbInstance().notesDao().updateNote(note);
            }
        });
    }
}
