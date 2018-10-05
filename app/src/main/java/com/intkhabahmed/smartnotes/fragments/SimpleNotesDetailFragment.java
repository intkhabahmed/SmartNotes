package com.intkhabahmed.smartnotes.fragments;

import android.arch.lifecycle.Observer;
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

import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.ui.AddSimpleNote;
import com.intkhabahmed.smartnotes.utils.NoteUtils;

public class SimpleNotesDetailFragment extends Fragment {

    private Note mNote;
    private static final String BUNDLE_DATA = "bundle-data";
    private TextView noteTitleTextView;
    private TextView noteDescriptionTextView;
    private TextView noteCreatedDateTextView;
    private TextView noteModifiedDateTextView;
    private FloatingActionButton editButton;


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
        noteTitleTextView = view.findViewById(R.id.tv_note_title);
        noteDescriptionTextView = view.findViewById(R.id.tv_note_desciption);
        noteCreatedDateTextView = view.findViewById(R.id.tv_date_created);
        noteModifiedDateTextView = view.findViewById(R.id.tv_date_modified);
        editButton = view.findViewById(R.id.edit_note_button);
        setupUI();
    }

    private void setupUI() {
        noteTitleTextView.setText(mNote.getNoteTitle());
        noteDescriptionTextView.setText(mNote.getDescription());
        noteCreatedDateTextView.setText(NoteUtils.getFormattedTime(mNote.getDateCreated()));
        noteModifiedDateTextView.setText(NoteUtils.getFormattedTime(mNote.getDateModified()));
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
    public void onResume() {
        super.onResume();
        NoteRepository.getInstance().getNoteById(mNote.getNoteId()).observe(this, new Observer<Note>() {
            @Override
            public void onChanged(@Nullable Note note) {
                mNote = note;
                setupUI();
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(BUNDLE_DATA, mNote);
        super.onSaveInstanceState(outState);
    }
}
