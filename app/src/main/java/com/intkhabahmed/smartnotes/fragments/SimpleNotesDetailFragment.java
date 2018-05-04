package com.intkhabahmed.smartnotes.fragments;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.notesdata.NotesContract;
import com.intkhabahmed.smartnotes.utils.NotesDateUtil;

public class SimpleNotesDetailFragment extends Fragment {

    private long mNoteId;
    private String noteTitle;
    private String noteDescription;
    private String noteCreatedDate;
    private String noteModifiedDate;
    private static final String BUNDLE_DATA = "bundle-data";


    public SimpleNotesDetailFragment() {
    }

    public void setNoteId(long noteId) {
        mNoteId = noteId;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.note_detail_layout, container, false);

        TextView noteTitleTextView = rootView.findViewById(R.id.tv_note_title);
        TextView noteDescriptionTextView = rootView.findViewById(R.id.tv_note_desciption);
        TextView noteCreatedDateTextView = rootView.findViewById(R.id.tv_date_created);
        TextView noteModifiedDateTextView = rootView.findViewById(R.id.tv_date_modified);
        if (savedInstanceState != null) {
            mNoteId = savedInstanceState.getLong(BUNDLE_DATA);
        }

        handleCursorData();
        noteTitleTextView.setText(noteTitle);
        noteDescriptionTextView.setText(noteDescription);
        noteCreatedDateTextView.setText(noteCreatedDate);
        noteModifiedDateTextView.setText(noteModifiedDate);
        return rootView;
    }

    private void handleCursorData() {
        Cursor cursor = getNoteDataById();
        cursor.moveToFirst();
        noteTitle = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_TITLE));
        noteDescription = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DESCRIPTION));
        noteCreatedDate = NotesDateUtil.getFormattedTime(cursor.getLong(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DATE_CREATED)));
        noteModifiedDate = NotesDateUtil.getFormattedTime(cursor.getLong(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DATE_MODIFIED)));
        cursor.close();
    }

    private Cursor getNoteDataById() {
        Uri singleNoteUri = ContentUris.withAppendedId(NotesContract.NotesEntry.CONTENT_URI, mNoteId).buildUpon().appendPath("0").build();
        return getActivity().getContentResolver().query(singleNoteUri, null,
                null, null, null);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(BUNDLE_DATA, mNoteId);
        super.onSaveInstanceState(outState);
    }
}
