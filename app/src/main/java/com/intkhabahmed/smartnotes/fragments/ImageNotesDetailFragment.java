package com.intkhabahmed.smartnotes.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.intkhabahmed.smartnotes.AddImageNote;
import com.intkhabahmed.smartnotes.AddSimpleNote;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.notesdata.NotesContract;
import com.intkhabahmed.smartnotes.utils.DBUtils;
import com.intkhabahmed.smartnotes.utils.NoteUtils;

import java.io.File;

public class ImageNotesDetailFragment extends Fragment {

    private long mNoteId;
    private String mNoteTitle;
    private String mImagePath;
    private String mNoteCreatedDate;
    private String mNoteModifiedDate;
    private static final String BUNDLE_DATA = "bundle-data";


    public ImageNotesDetailFragment() {
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
        noteDescriptionTextView.setVisibility(View.GONE);
        TextView noteCreatedDateTextView = view.findViewById(R.id.tv_date_created);
        TextView noteModifiedDateTextView = view.findViewById(R.id.tv_date_modified);
        ImageView noteImageView = view.findViewById(R.id.image_note_view);
        noteImageView.setVisibility(View.VISIBLE);
        if (savedInstanceState != null) {
            mNoteId = savedInstanceState.getLong(BUNDLE_DATA);
        }
        handleCursorData();
        File imageFile = new File(mImagePath);
        if (imageFile.exists()) {
            Glide.with(getActivity()).load(Uri.fromFile(imageFile)).into(noteImageView);
        }
        noteTitleTextView.setText(mNoteTitle);
        noteCreatedDateTextView.setText(mNoteCreatedDate);
        noteModifiedDateTextView.setText(mNoteModifiedDate);
        FloatingActionButton editButton = view.findViewById(R.id.edit_note_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddImageNote.class);
                intent.putExtra(Intent.EXTRA_TEXT, mNoteId);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    private void handleCursorData() {
        Cursor cursor = DBUtils.getNoteDataById(getActivity(), mNoteId);
        cursor.moveToFirst();
        mNoteTitle = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_TITLE));
        mImagePath = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DESCRIPTION));
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
