package com.intkhabahmed.smartnotes.notesdata;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by INTKHAB on 21-03-2018.
 */

public class NotesContract {
    public static final String CONTENT_AUTHORITY = "com.intkhabahmed.smartnotes";
    public static final Uri BASE_URI = Uri.parse("content://"+ CONTENT_AUTHORITY);
    public static final String NOTES_PATH = "notes";

    public static class NotesEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(NOTES_PATH).build();
        public static final String TABLE_NAME = "notes";

        //Table columns
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_TYPE = "noteType";
        public static final String COLUMN_DATE_CREATED = "dateCreated";
        public static final String COLUMN_DATE_MODIFIED = "dateModified";
        public static final String COLUMN_TRASH = "trash";
    }
}
