package com.intkhabahmed.smartnotes.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.adapters.NotesAdapter;
import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.databinding.NotesRecyclerViewBinding;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.ui.MainActivity;
import com.intkhabahmed.smartnotes.utils.AppExecutors;
import com.intkhabahmed.smartnotes.utils.BitmapUtils;
import com.intkhabahmed.smartnotes.utils.CurrentFragmentListener;
import com.intkhabahmed.smartnotes.utils.ViewUtils;
import com.intkhabahmed.smartnotes.viewmodels.NotesViewModel;
import com.intkhabahmed.smartnotes.viewmodels.NotesViewModelFactory;

public class TrashFragment extends Fragment implements NotesAdapter.OnItemClickListener {

    private NotesAdapter mNotesAdapter;
    private RecyclerView mRecyclerView;
    private NotesRecyclerViewBinding mNotesBinding;

    public TrashFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        getParentActivity().setTitle(R.string.trash);
        setupViewModel();
    }

    private void setupViewModel() {
        mNotesBinding.progressBar.setVisibility(View.VISIBLE);
        NotesViewModelFactory factory = new NotesViewModelFactory(null, 1);
        NotesViewModel notesViewModel = ViewModelProviders.of(this, factory).get(NotesViewModel.class);
        notesViewModel.getNotes().observe(this, new Observer<PagedList<Note>>() {
            @Override
            public void onChanged(@Nullable PagedList<Note> notes) {
                mNotesBinding.progressBar.setVisibility(View.GONE);
                mNotesAdapter.submitList(notes);
                if (notes != null && notes.size() > 0) {
                    ViewUtils.hideEmptyView(mRecyclerView, mNotesBinding.trashEmptyView);
                } else {
                    setHasOptionsMenu(false);
                    ViewUtils.showEmptyView(mRecyclerView, mNotesBinding.trashEmptyView);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        CurrentFragmentListener listener = ((MainActivity) getParentActivity()).getCurrentFragmentListener();
        listener.setCurrentFragment(TrashFragment.class.getSimpleName());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.trash_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.empty_trash) {
            DialogInterface.OnClickListener deleteListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    NoteRepository.getInstance().emptyTrash();
                    Toast.makeText(getParentActivity(), getString(R.string.all_items_deleted), Toast.LENGTH_LONG).show();
                }
            };
            ViewUtils.showDeleteConfirmationDialog(getContext(), deleteListener, getString(R.string.empty_trash_dialog_message));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(int noteId, @NonNull String noteType) {
        Fragment fragment;
        if (noteType.equals(getString(R.string.checklist))) {
            ChecklistNotesDetailFragment checklistNotesDetailFragment = new ChecklistNotesDetailFragment();
            checklistNotesDetailFragment.setNoteId(noteId);
            fragment = checklistNotesDetailFragment;
        } else if (noteType.equals(getString(R.string.image_note))) {
            ImageNotesDetailFragment imageNotesDetailFragment = new ImageNotesDetailFragment();
            imageNotesDetailFragment.setNoteId(noteId);
            fragment = imageNotesDetailFragment;
        } else {
            SimpleNotesDetailFragment simpleNotesDetailFragment = new SimpleNotesDetailFragment();
            simpleNotesDetailFragment.setNoteId(noteId);
            fragment = simpleNotesDetailFragment;
        }
        getParentActivity().getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragment_layout, fragment)
                .commit();
    }

    @Override
    public void onMenuItemClick(@NonNull View view, @NonNull final Note note) {
        PopupMenu popupMenu = new PopupMenu(getParentActivity(), view);
        popupMenu.inflate(R.menu.item_menu);
        popupMenu.getMenu().getItem(0).setTitle(getString(R.string.delete_forever));
        popupMenu.getMenu().getItem(1).setTitle(getString(R.string.restore));
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.delete_note:
                        DialogInterface.OnClickListener deleteListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (note.getNoteType() != null && note.getNoteType().equals(getString(R.string.image_note))) {
                                    String imagePath = note.getDescription();
                                    BitmapUtils.deleteImageFile(getParentActivity(), imagePath);
                                }
                                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        NoteRepository.getInstance().deleteNote(note);
                                    }
                                });
                                Toast.makeText(getParentActivity(), getString(R.string.deleted_permanently), Toast.LENGTH_LONG).show();
                            }
                        };
                        ViewUtils.showDeleteConfirmationDialog(getContext(), deleteListener, getString(R.string.permanently_delete_dialog_message));
                        break;
                    case R.id.share_note:
                        NoteRepository.getInstance().recoverNoteFromTrash(note);
                        Toast.makeText(getParentActivity(), getString(R.string.restored), Toast.LENGTH_LONG).show();
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private FragmentActivity getParentActivity() {
        return getActivity();
    }
}
