package com.intkhabahmed.smartnotes.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.intkhabahmed.smartnotes.models.Note;

@Database(entities = {Note.class}, version = 1)
public abstract class NotesDatabase extends RoomDatabase {
    private static final Object LOCK = new Object();
    private static NotesDatabase sInstance;
    private static final String DATABASE_NAME = "smartNotes.db";

    public static NotesDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context, NotesDatabase.class, DATABASE_NAME)
                        .build();
            }
        }
        return sInstance;
    }

    public abstract NotesDao notesDao();
}
