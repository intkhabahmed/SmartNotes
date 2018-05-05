package com.intkhabahmed.smartnotes.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.notesdata.NotesContract;
import com.intkhabahmed.smartnotes.utils.DBUtils;
import com.intkhabahmed.smartnotes.utils.NoteUtils;

public class SimpleNotesDetailFragment extends Fragment {

    private long mNoteId;
    private String mNoteTitle;
    private String mNoteDescription;
    private String mNoteCreatedDate;
    private String mNoteModifiedDate;
    private static final String BUNDLE_DATA = "bundle-data";


    public SimpleNotesDetailFragment() {
    }

    public void setNoteId(long noteId) {
        mNoteId = noteId;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.note_detail_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView noteTitleTextView = view.findViewById(R.id.tv_note_title);
        TextView noteDescriptionTextView = view.findViewById(R.id.tv_note_desciption);
        TextView noteCreatedDateTextView = view.findViewById(R.id.tv_date_created);
        TextView noteModifiedDateTextView = view.findViewById(R.id.tv_date_modified);
        if (savedInstanceState != null) {
            mNoteId = savedInstanceState.getLong(BUNDLE_DATA);
        }
        handleCursorData();
        noteTitleTextView.setText(mNoteTitle);
        noteDescriptionTextView.setText(mNoteDescription);
        noteCreatedDateTextView.setText(mNoteCreatedDate);
        noteModifiedDateTextView.setText(mNoteModifiedDate);
    }

    private void handleCursorData() {
        Cursor cursor = DBUtils.getNoteDataById(getActivity(), mNoteId);
        cursor.moveToFirst();
        mNoteTitle = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_TITLE));
        mNoteDescription = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DESCRIPTION));
        mNoteCreatedDate = NoteUtils.getFormattedTime(cursor.getLong(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DATE_CREATED)));
        mNoteModifiedDate = NoteUtils.getFormattedTime(cursor.getLong(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DATE_MODIFIED)));
        cursor.close();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(BUNDLE_DATA, mNoteId);
        super.onSaveInstanceState(outState);
    }
}
