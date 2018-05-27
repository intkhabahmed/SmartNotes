package com.intkhabahmed.smartnotes.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.intkhabahmed.smartnotes.notesdata.NotesContract;

public class DBUtils {
    public static void moveToTrash(Context context, long noteId) {
        ContentValues values = new ContentValues();
        values.put(NotesContract.NotesEntry.COLUMN_TRASH, 1);
        context.getContentResolver().update(NotesContract.NotesEntry.CONTENT_URI, values,
                NotesContract.NotesEntry._ID + "=?", new String[]{String.valueOf(noteId)});
    }

    public static void restoreFromTrash(Context context, long noteId) {
        ContentValues values = new ContentValues();
        values.put(NotesContract.NotesEntry.COLUMN_TRASH, 0);
        context.getContentResolver().update(NotesContract.NotesEntry.CONTENT_URI, values,
                NotesContract.NotesEntry._ID + "=?", new String[]{String.valueOf(noteId)});
    }

    public static Cursor getNoteDataById(Context context, long noteId) {
        String selection = NotesContract.NotesEntry._ID + "=?";
        String[] selectionArgs = {String.valueOf(noteId)};
        return context.getContentResolver().query(NotesContract.NotesEntry.CONTENT_URI, null,
                selection, selectionArgs, null);
    }
}
