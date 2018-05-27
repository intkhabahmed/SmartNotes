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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.notesdata.NotesContract;
import com.intkhabahmed.smartnotes.utils.NotesDateUtil;

import java.io.File;

public class ImageNotesDetailFragment extends Fragment {

    private long mNoteId;
    private String noteTitle;
    private String imagePath;
    private String noteCreatedDate;
    private String noteModifiedDate;
    private static final String BUNDLE_DATA = "bundle-data";
    private TextView noteDescriptionTextView;
    private ImageView noteImageView;


    public ImageNotesDetailFragment() {
    }

    public void setNoteId(long noteId){
        mNoteId = noteId;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.note_detail_layout, container, false);

        TextView noteTitleTextView = rootView.findViewById(R.id.tv_note_title);
        noteDescriptionTextView = rootView.findViewById(R.id.tv_note_desciption);
        noteDescriptionTextView.setVisibility(View.GONE);
        TextView noteCreatedDateTextView = rootView.findViewById(R.id.tv_date_created);
        TextView noteModifiedDateTextView = rootView.findViewById(R.id.tv_date_modified);
        noteImageView = rootView.findViewById(R.id.image_note_view);
        noteImageView.setVisibility(View.VISIBLE);
        if(savedInstanceState != null){
            mNoteId = savedInstanceState.getLong(BUNDLE_DATA);
        }

        handleCursorData();
        File imageFile = new File(imagePath);
        if(imageFile.exists()){
            Glide.with(getActivity()).load(Uri.fromFile(imageFile)).into(noteImageView);
        }
        noteTitleTextView.setText(noteTitle);
        noteCreatedDateTextView.setText(noteCreatedDate);
        noteModifiedDateTextView.setText(noteModifiedDate);
        return rootView;
    }

    private void handleCursorData() {
        Cursor cursor = getNoteDataById();
        cursor.moveToFirst();
        noteTitle = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_TITLE));
        imagePath = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DESCRIPTION));
        noteCreatedDate = NotesDateUtil.getFormattedTime(cursor.getLong(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DATE_CREATED)));
        noteModifiedDate = NotesDateUtil.getFormattedTime(cursor.getLong(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DATE_MODIFIED)));
        cursor.close();
    }

    private Cursor getNoteDataById(){
        Uri singleNoteUri = ContentUris.withAppendedId(NotesContract.NotesEntry.CONTENT_URI, mNoteId).buildUpon().appendPath("0").build();
        return getActivity().getContentResolver().query(singleNoteUri, null,
                null, null, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        noteDescriptionTextView.setVisibility(View.GONE);
        noteImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(BUNDLE_DATA, mNoteId);
        super.onSaveInstanceState(outState);
    }
}
