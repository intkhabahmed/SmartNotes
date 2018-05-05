package com.intkhabahmed.smartnotes.utils;

import android.content.ContentValues;
import android.content.Context;

import com.intkhabahmed.smartnotes.notesdata.NotesContract;

public class DBUtils {
    public static void moveToTrash(Context context, int noteId) {
        ContentValues values = new ContentValues();
        values.put(NotesContract.NotesEntry.COLUMN_TRASH, 1);
        context.getContentResolver().update(NotesContract.NotesEntry.CONTENT_URI, values,
                NotesContract.NotesEntry._ID + "=?", new String[]{String.valueOf(noteId)});
    }

    public static void restoreFromTrash(Context context, int noteId) {
        ContentValues values = new ContentValues();
        values.put(NotesContract.NotesEntry.COLUMN_TRASH, 0);
        context.getContentResolver().update(NotesContract.NotesEntry.CONTENT_URI, values,
                NotesContract.NotesEntry._ID + "=?", new String[]{String.valueOf(noteId)});
    }
}
