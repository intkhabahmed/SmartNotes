package com.intkhabahmed.smartnotes.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

import com.intkhabahmed.smartnotes.models.Note;

@Database(entities = {Note.class}, version = 3)
public abstract class NotesDatabase extends RoomDatabase {
    private static final Object LOCK = new Object();
    private static NotesDatabase sInstance;
    private static final String DATABASE_NAME = "smartNotes.db";

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            //Structure is same
        }
    };
    public static NotesDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context, NotesDatabase.class, DATABASE_NAME)
                        .addMigrations(MIGRATION_2_3)
                        .build();
            }
        }
        return sInstance;
    }

    public abstract NotesDao notesDao();
}
