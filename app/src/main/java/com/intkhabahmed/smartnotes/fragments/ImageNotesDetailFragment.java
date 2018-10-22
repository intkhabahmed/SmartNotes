package com.intkhabahmed.smartnotes.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.ui.AddImageNote;
import com.intkhabahmed.smartnotes.utils.NoteUtils;
import com.intkhabahmed.smartnotes.utils.ViewUtils;
import com.intkhabahmed.smartnotes.viewmodels.NoteViewModel;
import com.intkhabahmed.smartnotes.viewmodels.NoteViewModelFactory;

import java.io.File;

public class ImageNotesDetailFragment extends Fragment {

    private Note mNote;
    private int mNoteId;
    private static final String BUNDLE_DATA = "bundle-data";
    private TextView noteTitleTextView;
    private TextView noteCreatedDateTextView;
    private TextView noteModifiedDateTextView;
    private ImageView noteImageView;
    private FloatingActionButton editButton;


    public ImageNotesDetailFragment() {
    }

    public void setNoteId(int noteId) {
        mNoteId = noteId;


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mNoteId = savedInstanceState.getInt(BUNDLE_DATA);
        }
        setHasOptionsMenu(true);
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
        noteCreatedDateTextView = view.findViewById(R.id.tv_date_created);
        noteModifiedDateTextView = view.findViewById(R.id.tv_date_modified);
        noteImageView = view.findViewById(R.id.image_note_view);
        editButton = view.findViewById(R.id.edit_note_button);
        setupNoteViewModel();
    }

    private void setupNoteViewModel() {
        NoteViewModelFactory factory = new NoteViewModelFactory(mNoteId);
        NoteViewModel noteViewModel = ViewModelProviders.of(this, factory).get(NoteViewModel.class);
        noteViewModel.getNote().observe(this, new Observer<Note>() {
            @Override
            public void onChanged(@Nullable Note note) {
                if (note != null) {
                    mNote = note;
                    if (mNote.getTrashed() == 1) {
                        setHasOptionsMenu(false);
                    }
                    setupUI();
                }
            }
        });
    }

    private void setupUI() {
        noteImageView.setVisibility(View.VISIBLE);
        File imageFile = new File(mNote.getDescription());
        if (imageFile.exists()) {
            Glide.with(getActivity()).load(Uri.fromFile(imageFile)).into(noteImageView);
        }
        noteTitleTextView.setText(mNote.getNoteTitle());
        noteCreatedDateTextView.setText(NoteUtils.getFormattedTime(mNote.getDateCreated()));
        noteModifiedDateTextView.setText(NoteUtils.getFormattedTime(mNote.getDateModified()));
        if (mNote.getTrashed() == 1) {
            editButton.setVisibility(View.GONE);
        }
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddImageNote.class);
                intent.putExtra(Intent.EXTRA_TEXT, mNote);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(BUNDLE_DATA, mNoteId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete_menu) {
            DialogInterface.OnClickListener deleteListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    NoteRepository.getInstance().moveNoteToTrash(mNote);
                    Toast.makeText(getActivity(), getString(R.string.moved_to_trash), Toast.LENGTH_LONG).show();
                    getActivity().getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            };
            ViewUtils.showDeleteConfirmationDialog(getContext(), deleteListener);
        }
        return super.onOptionsItemSelected(item);
    }
}
