package com.intkhabahmed.smartnotes.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.intkhabahmed.smartnotes.AddAndEditChecklist;
import com.intkhabahmed.smartnotes.NotesAdapter;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.utils.NoteUtils;
import com.intkhabahmed.smartnotes.utils.ViewUtils;
import com.intkhabahmed.smartnotes.viewmodels.NotesViewModel;
import com.intkhabahmed.smartnotes.viewmodels.NotesViewModelFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by INTKHAB on 23-03-2018.
 */

public class ChecklistFragment extends Fragment implements NotesAdapter.OnItemClickListener {

    private NotesAdapter mNotesAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayout mEmptyView;
    private ProgressBar mProgressBar;
    private FloatingActionButton mAddButton;

    public ChecklistFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notes_recycler_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mEmptyView = view.findViewById(R.id.empty_view);
        mProgressBar = view.findViewById(R.id.progress_bar);
        mNotesAdapter = new NotesAdapter(getActivity(), this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mNotesAdapter);
        mRecyclerView.setHasFixedSize(true);
        mEmptyView.setVisibility(View.INVISIBLE);
        mAddButton = view.findViewById(R.id.add_button);
        mAddButton.setVisibility(View.VISIBLE);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddAndEditChecklist.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        setupViewModel();
    }

    private void setupViewModel() {
        mProgressBar.setVisibility(View.VISIBLE);
        NotesViewModelFactory factory = new NotesViewModelFactory(getString(R.string.checklist), 0);
        NotesViewModel notesViewModel = ViewModelProviders.of(this, factory).get(NotesViewModel.class);
        notesViewModel.getNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(@Nullable List<Note> notes) {
                if (notes != null) {
                    mProgressBar.setVisibility(View.GONE);
                    ViewUtils.hideEmptyView(mRecyclerView, mEmptyView);
                    mNotesAdapter.setNotes(notes);
                } else {
                    mNotesAdapter.setNotes(null);
                    ViewUtils.showEmptyView(mRecyclerView, mEmptyView);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mAddButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClick(Note note) {
        Intent editChecklistActivityIntent = new Intent(getActivity(), AddAndEditChecklist.class);
        editChecklistActivityIntent.putExtra(Intent.EXTRA_TEXT, note);
        startActivity(editChecklistActivityIntent);
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onMenuItemClick(View view, final Note note) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
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
                        ViewUtils.showDeleteConfirmationDialog(getActivity(), deleteListener);
                        break;
                    case R.id.share_note:
                        String noteTitle = note.getNoteTitle();
                        String noteDescription = note.getDescription();
                        StringBuilder tasks = new StringBuilder();
                        tasks.append(noteTitle);
                        tasks.append("\n_____________________");
                        try {
                            JSONObject checklistObjects = new JSONObject(noteDescription);
                            JSONArray jsonArrays = checklistObjects.getJSONArray(getActivity().getString(R.string.checklist));
                            for (int i = 0; i < jsonArrays.length(); i++) {
                                try {
                                    JSONObject jsonObject = jsonArrays.getJSONObject(i);
                                    tasks.append("\n");
                                    tasks.append(jsonObject.getString(AddAndEditChecklist.LIST_TITLE));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        NoteUtils.shareNote(getActivity(), tasks.toString());

                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    public void updateCheckListFragment() {

    }

    private void showSnackBar(final Note note) {
        Snackbar snackbar = Snackbar.make(mAddButton, "Note has been moved to trash", Snackbar.LENGTH_LONG);
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NoteRepository.getInstance().recoverNoteFromTrash(note);
                Snackbar.make(mAddButton, "Note Restored", Snackbar.LENGTH_LONG).show();
            }
        });
        snackbar.setActionTextColor(ViewUtils.getColorFromAttribute(getActivity(), R.attr.colorAccent));
        snackbar.show();
    }
}

