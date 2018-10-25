package com.intkhabahmed.smartnotes.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.databinding.NoteDetailLayoutBinding;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.ui.AddSimpleNote;
import com.intkhabahmed.smartnotes.utils.NoteUtils;
import com.intkhabahmed.smartnotes.utils.ViewUtils;
import com.intkhabahmed.smartnotes.viewmodels.NoteViewModel;
import com.intkhabahmed.smartnotes.viewmodels.NoteViewModelFactory;

public class SimpleNotesDetailFragment extends Fragment {

    private Note mNote;
    private int mNoteId;
    private static final String BUNDLE_DATA = "bundle-data";
    private NoteDetailLayoutBinding mDetailBinding;


    public SimpleNotesDetailFragment() {
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
        mDetailBinding = DataBindingUtil.inflate(inflater, R.layout.note_detail_layout, container, false);
        return mDetailBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        mDetailBinding.tvNoteDescription.setVisibility(View.VISIBLE);
        mDetailBinding.tvNoteTitle.setText(mNote.getNoteTitle());
        mDetailBinding.tvNoteDescription.setText(mNote.getDescription());
        mDetailBinding.tvDateCreated.setText(NoteUtils.getFormattedTime(mNote.getDateCreated()));
        mDetailBinding.tvDateModified.setText(NoteUtils.getFormattedTime(mNote.getDateModified()));
        if (mNote.getTrashed() == 1) {
            mDetailBinding.editNoteButton.setVisibility(View.GONE);
        }

        mDetailBinding.editNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getParentActivity(), AddSimpleNote.class);
                intent.putExtra(Intent.EXTRA_TEXT, mNote);
                startActivity(intent);
                getParentActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
                    Toast.makeText(getParentActivity(), getString(R.string.moved_to_trash), Toast.LENGTH_LONG).show();
                    getParentActivity().getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            };
            ViewUtils.showDeleteConfirmationDialog(getContext(), deleteListener);
        }
        return super.onOptionsItemSelected(item);
    }

    public FragmentActivity getParentActivity() {
        return getActivity();
    }
}
