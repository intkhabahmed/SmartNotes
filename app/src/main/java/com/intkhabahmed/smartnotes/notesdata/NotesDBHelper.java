package com.intkhabahmed.smartnotes.notesdata;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.intkhabahmed.smartnotes.notesdata.NotesContract.NotesEntry.*;

/**
 * Created by INTKHAB on 21-03-2018.
 */

public class NotesDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "smartNotes";

    NotesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( "
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TITLE + " TEXT NOT NULL, "
                + COLUMN_DESCRIPTION + " TEXT NOT NULL, "
                + COLUMN_TYPE + " TEXT NOT NULL, "
                + COLUMN_DATE_CREATED + " INTEGER NOT NULL, "
                + COLUMN_DATE_MODIFIED + " INTEGER NOT NULL, "
                + COLUMN_TRASH + " INTEGER NOT NULL DEFAULT 0 );";
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
