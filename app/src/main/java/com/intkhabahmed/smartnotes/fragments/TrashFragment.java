package com.intkhabahmed.smartnotes.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.Toast;

import com.intkhabahmed.smartnotes.AddAndEditChecklist;
import com.intkhabahmed.smartnotes.NoteDetailActivity;
import com.intkhabahmed.smartnotes.NotesAdapter;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.utils.AppExecutors;
import com.intkhabahmed.smartnotes.utils.BitmapUtils;
import com.intkhabahmed.smartnotes.utils.ViewUtils;
import com.intkhabahmed.smartnotes.viewmodels.NotesViewModel;
import com.intkhabahmed.smartnotes.viewmodels.NotesViewModelFactory;

import java.util.List;

public class TrashFragment extends Fragment implements NotesAdapter.OnItemClickListener {

    private NotesAdapter mNotesAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayout mEmptyView;
    private ProgressBar mProgressBar;

    public TrashFragment() {
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
        mEmptyView = view.findViewById(R.id.trash_empty_view);
        mProgressBar = view.findViewById(R.id.progress_bar);

        mNotesAdapter = new NotesAdapter(getActivity(), this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mNotesAdapter);
        mRecyclerView.setHasFixedSize(true);
        mProgressBar.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.INVISIBLE);
        getActivity().setTitle(R.string.trash);
        setupViewModel();
    }

    private void setupViewModel() {
        mProgressBar.setVisibility(View.VISIBLE);
        NotesViewModelFactory factory = new NotesViewModelFactory(getString(R.string.simple_note), 0);
        NotesViewModel notesViewModel = ViewModelProviders.of(this, factory).get(NotesViewModel.class);
        notesViewModel.getNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(@Nullable List<Note> notes) {
                mProgressBar.setVisibility(View.GONE);
                if (notes != null && notes.size() > 0) {
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
    public void onItemClick(Note note) {
        String noteType = note.getNoteType();
        Intent detailActivityIntent;
        if (noteType.equals(getString(R.string.checklist))) {
            detailActivityIntent = new Intent(getActivity(), AddAndEditChecklist.class);
        } else {
            detailActivityIntent = new Intent(getActivity(), NoteDetailActivity.class);
            detailActivityIntent.putExtra(getString(R.string.note_type), noteType);
        }
        detailActivityIntent.putExtra(Intent.EXTRA_TEXT, note);
        startActivity(detailActivityIntent);
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onMenuItemClick(View view, final Note note) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        popupMenu.inflate(R.menu.item_menu);
        popupMenu.getMenu().getItem(0).setTitle(getString(R.string.delete_forever));
        popupMenu.getMenu().getItem(1).setTitle(getString(R.string.restore));
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.delete_note:
                        String imagePath = note.getDescription();
                        BitmapUtils.deleteImageFile(getActivity(), imagePath);
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                NoteRepository.getInstance().deleteNote(note);
                            }
                        });
                        Toast.makeText(getActivity(), getString(R.string.deleted_permanently), Toast.LENGTH_LONG).show();
                        break;
                    case R.id.share_note:
                        NoteRepository.getInstance().recoverNoteFromTrash(note);
                        Toast.makeText(getActivity(), getString(R.string.restored), Toast.LENGTH_LONG).show();
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }
}
