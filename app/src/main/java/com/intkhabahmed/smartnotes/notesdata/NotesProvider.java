package com.intkhabahmed.smartnotes.notesdata;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by INTKHAB on 21-03-2018.
 */

public class NotesProvider extends ContentProvider {

    private static final int PATH_NOTES = 100;
    private static final int PATH_NOTE_ID_TRASH = 101;
    private static final int PATH_NOTE_TITLE = 102;
    private static UriMatcher sUriMatcher = buildUriMatcher();
    private NotesDBHelper mNotesDBHelper;

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(NotesContract.CONTENT_AUTHORITY, NotesContract.NOTES_PATH, PATH_NOTES);
        //PATH_NOTE_ID_TRASH : "com.intkhabahmed.smartnotes/notes/id/isTrashed
        uriMatcher.addURI(NotesContract.CONTENT_AUTHORITY, NotesContract.NOTES_PATH + "/#/#", PATH_NOTE_ID_TRASH);
        uriMatcher.addURI(NotesContract.CONTENT_AUTHORITY, NotesContract.NOTES_PATH + "/*", PATH_NOTE_TITLE);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mNotesDBHelper = new NotesDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mNotesDBHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCursor = null;
        switch (match) {
            case PATH_NOTES:
                retCursor = db.query(NotesContract.NotesEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PATH_NOTE_ID_TRASH:
                selection = NotesContract.NotesEntry._ID + "=? AND "+ NotesContract.NotesEntry.COLUMN_TRASH + "=?";
                selectionArgs = new String[]{String.valueOf(uri.getPathSegments().get(1)),String.valueOf(uri.getPathSegments().get(2))};
                retCursor = db.query(NotesContract.NotesEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PATH_NOTE_TITLE:
                selection = NotesContract.NotesEntry.COLUMN_TITLE + "like %?%";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                retCursor = db.query(NotesContract.NotesEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri "+ uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(),uri);
        return retCursor;

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        SQLiteDatabase db = mNotesDBHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri = null;
        switch (match) {
            case PATH_NOTES:
                long rowId = db.insert(NotesContract.NotesEntry.TABLE_NAME, null, contentValues);
                if(rowId > 0){
                    returnUri = ContentUris.withAppendedId(NotesContract.NotesEntry.CONTENT_URI, rowId);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri "+ uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mNotesDBHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case PATH_NOTES:
                rowsDeleted = db.delete(NotesContract.NotesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PATH_NOTE_ID_TRASH:
                selection = NotesContract.NotesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(uri.getLastPathSegment())};
                rowsDeleted = db.delete(NotesContract.NotesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri "+ uri);
        }
        if(rowsDeleted > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mNotesDBHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case PATH_NOTES:
                rowsUpdated = db.update(NotesContract.NotesEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case PATH_NOTE_ID_TRASH:
                selection = NotesContract.NotesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(uri.getLastPathSegment())};
                rowsUpdated = db.delete(NotesContract.NotesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri "+ uri);
        }
        if(rowsUpdated > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
