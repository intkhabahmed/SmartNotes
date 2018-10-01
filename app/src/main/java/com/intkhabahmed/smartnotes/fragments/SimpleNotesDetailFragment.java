package com.intkhabahmed.smartnotes.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.intkhabahmed.smartnotes.AddSimpleNote;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.utils.NoteUtils;

public class SimpleNotesDetailFragment extends Fragment {

    private Note mNote;
    private static final String BUNDLE_DATA = "bundle-data";


    public SimpleNotesDetailFragment() {
    }

    public void setNote(Note note) {
        mNote = note;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mNote = savedInstanceState.getParcelable(BUNDLE_DATA);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.note_detail_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView noteTitleTextView = view.findViewById(R.id.tv_note_title);
        TextView noteDescriptionTextView = view.findViewById(R.id.tv_note_desciption);
        TextView noteCreatedDateTextView = view.findViewById(R.id.tv_date_created);
        TextView noteModifiedDateTextView = view.findViewById(R.id.tv_date_modified);
        noteTitleTextView.setText(mNote.getNoteTitle());
        noteDescriptionTextView.setText(mNote.getDescription());
        noteCreatedDateTextView.setText(NoteUtils.getFormattedTime(mNote.getDateCreated()));
        noteModifiedDateTextView.setText(NoteUtils.getFormattedTime(mNote.getDateModified()));
        FloatingActionButton editButton = view.findViewById(R.id.edit_note_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddSimpleNote.class);
                intent.putExtra(Intent.EXTRA_TEXT, mNote);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(BUNDLE_DATA, mNote);
        super.onSaveInstanceState(outState);
    }
}
