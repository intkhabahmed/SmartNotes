package com.intkhabahmed.smartnotes.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.adapters.NotesAdapter;
import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.databinding.NotesRecyclerViewBinding;
import com.intkhabahmed.smartnotes.models.ChecklistItem;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.ui.AddAndEditChecklist;
import com.intkhabahmed.smartnotes.utils.NoteUtils;
import com.intkhabahmed.smartnotes.utils.ViewUtils;
import com.intkhabahmed.smartnotes.viewmodels.NotesViewModel;
import com.intkhabahmed.smartnotes.viewmodels.NotesViewModelFactory;

import java.util.List;

/**
 * Created by INTKHAB on 01-10-2018.
 */

public class ChecklistFragment extends Fragment implements NotesAdapter.OnItemClickListener {

    private NotesAdapter mNotesAdapter;
    private RecyclerView mRecyclerView;
    private NotesRecyclerViewBinding mNotesBinding;

    public ChecklistFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mNotesBinding = DataBindingUtil.inflate(inflater, R.layout.notes_recycler_view, container, false);
        return mNotesBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = mNotesBinding.recyclerView;
        mNotesAdapter = new NotesAdapter(getParentActivity(), this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getParentActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mNotesAdapter);
        mRecyclerView.setHasFixedSize(true);
        mNotesBinding.addButton.show();
        mNotesBinding.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getParentActivity(), AddAndEditChecklist.class);
                startActivity(intent);
                getParentActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        setupViewModel(false);
    }

    private void setupViewModel(boolean isSortCriteriaChanged) {
        mNotesBinding.progressBar.setVisibility(View.VISIBLE);
        NotesViewModelFactory factory = new NotesViewModelFactory(getString(R.string.checklist), 0);
        NotesViewModel notesViewModel = ViewModelProviders.of(this, factory).get(NotesViewModel.class);
        if (isSortCriteriaChanged) {
            notesViewModel.setNotes(getString(R.string.checklist), 0);
        }
        notesViewModel.getNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(@Nullable List<Note> notes) {
                mNotesBinding.progressBar.setVisibility(View.GONE);
                if (notes != null && notes.size() > 0) {
                    ViewUtils.hideEmptyView(mRecyclerView, mNotesBinding.emptyView);
                    mNotesAdapter.setNotes(notes);
                } else {
                    mNotesAdapter.setNotes(null);
                    ViewUtils.showEmptyView(mRecyclerView, mNotesBinding.emptyView);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mNotesBinding.addButton.show();
    }

    @Override
    public void onItemClick(int noteId, String noteType) {
        ChecklistNotesDetailFragment checklistNotesDetailFragment = new ChecklistNotesDetailFragment();
        checklistNotesDetailFragment.setNoteId(noteId);
        getParentActivity().getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragment_layout, checklistNotesDetailFragment)
                .commit();
    }

    @Override
    public void onMenuItemClick(View view, final Note note) {
        PopupMenu popupMenu = new PopupMenu(getParentActivity(), view);
        popupMenu.inflate(R.menu.item_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.delete_note:
                        DialogInterface.OnClickListener deleteListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NoteRepository.getInstance().moveNoteToTrash(note);
                                showSnackBar(note);
                            }
                        };
                        ViewUtils.showDeleteConfirmationDialog(getContext(), deleteListener);
                        break;
                    case R.id.share_note:
                        String noteTitle = note.getNoteTitle();
                        StringBuilder tasks = new StringBuilder();
                        tasks.append(noteTitle);
                        tasks.append("\n_____________________");
                        List<ChecklistItem> checklistItems = new Gson().fromJson(note.getDescription(), new TypeToken<List<ChecklistItem>>() {
                        }.getType());
                        for (ChecklistItem item : checklistItems) {
                            tasks.append("\n");
                            tasks.append(item.getTitle());
                        }
                        NoteUtils.shareNote(getParentActivity(), tasks.toString());
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    public void updateCheckListFragment() {
        setupViewModel(true);

    }

    private void showSnackBar(final Note note) {
        Snackbar snackbar = Snackbar.make(mNotesBinding.addButton, getString(R.string.moved_to_trash), Snackbar.LENGTH_LONG);
        snackbar.setAction(getString(R.string.undo), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NoteRepository.getInstance().recoverNoteFromTrash(note);
                Snackbar.make(mNotesBinding.addButton, getString(R.string.restored), Snackbar.LENGTH_LONG).show();
            }
        });
        snackbar.setActionTextColor(ViewUtils.getColorFromAttribute(getParentActivity(), R.attr.colorAccent));
        snackbar.show();
    }

    private FragmentActivity getParentActivity() {
        return getActivity();
    }
}

