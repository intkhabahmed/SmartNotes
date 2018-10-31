package com.intkhabahmed.smartnotes.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.adapters.NotesAdapter;
import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.databinding.NotesRecyclerViewBinding;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.utils.NoteUtils;
import com.intkhabahmed.smartnotes.utils.ViewUtils;
import com.intkhabahmed.smartnotes.viewmodels.SearchNotesViewModel;
import com.intkhabahmed.smartnotes.viewmodels.SearchNotesViewModelFactory;

import java.util.List;

public class SearchFragment extends Fragment implements NotesAdapter.OnItemClickListener, SearchView.OnQueryTextListener {

    private NotesAdapter mNotesAdapter;
    private RecyclerView mRecyclerView;
    private SearchView mSearchView;
    private String mFilterText;
    private static final String BUNDLE_EXTRA = "search-query";
    private NotesRecyclerViewBinding mNotesBinding;

    public SearchFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mFilterText = savedInstanceState.getString(BUNDLE_EXTRA);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mNotesBinding = DataBindingUtil.inflate(inflater, R.layout.notes_recycler_view, container, false);
        return mNotesBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mRecyclerView = mNotesBinding.recyclerView;
        mNotesAdapter = new NotesAdapter(getParentActivity(), this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getParentActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mNotesAdapter);
        mRecyclerView.setHasFixedSize(true);
        setupViewModel(false);
        super.onViewCreated(view, savedInstanceState);
    }

    private void setupViewModel(boolean isQueryChanged) {
        SearchNotesViewModelFactory factory = new SearchNotesViewModelFactory(mFilterText, 0);
        SearchNotesViewModel notesViewModel = ViewModelProviders.of(this, factory).get(SearchNotesViewModel.class);
        if (isQueryChanged) {
            notesViewModel.setNotes(mFilterText, 0);
        }
        notesViewModel.getNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(@Nullable List<Note> notes) {
                if (notes != null && notes.size() > 0) {
                    hideEmptyView();
                    mNotesAdapter.setNotes(notes);
                } else {
                    mNotesAdapter.setNotes(null);
                    showEmptyView();
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getParentActivity().getMenuInflater().inflate(R.menu.search_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem searchViewItem = menu.findItem(R.id.search_menu);
        searchViewItem.expandActionView();
        mSearchView = (SearchView) menu.findItem(R.id.search_menu).getActionView();
        mSearchView.setQueryHint(getString(R.string.search_hint));
        EditText searchEditText = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setHintTextColor(Color.WHITE);
        searchEditText.setTextColor(Color.WHITE);
        ImageView closedBtn = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        closedBtn.setColorFilter(Color.WHITE);
        mSearchView.setMaxWidth(4000);
        mSearchView.setOnQueryTextListener(this);
        if (!TextUtils.isEmpty(mFilterText)) {
            mSearchView.setQuery(mFilterText, false);
        }
        searchViewItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                mSearchView.setQuery("", false);
                getParentActivity().getSupportFragmentManager().popBackStack(HomePageFragment.class.getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                return true;
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        mFilterText = TextUtils.isEmpty(query) ? null : query;
        setupViewModel(true);

        return true;
    }

    private void showEmptyView() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mNotesBinding.searchErrorView.setVisibility(View.INVISIBLE);
        if (!TextUtils.isEmpty(mFilterText)) {
            mNotesBinding.emptyView.setVisibility(View.VISIBLE);
        }
    }

    private void hideEmptyView() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mNotesBinding.searchErrorView.setVisibility(View.INVISIBLE);
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
                        NoteRepository.getInstance().moveNoteToTrash(note);
                        showSnackBar(note);
                        break;
                    case R.id.share_note:
                        String noteDescription = note.getDescription();
                        NoteUtils.shareNote(getParentActivity(), noteDescription);
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onItemClick(int noteId, String noteType) {
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

    private void showSnackBar(final Note note) {
        Snackbar snackbar = Snackbar.make(mNotesBinding.rootFrameLayout, getString(R.string.moved_to_trash), Snackbar.LENGTH_LONG);
        snackbar.setAction(getString(R.string.undo), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NoteRepository.getInstance().recoverNoteFromTrash(note);
                Snackbar.make(mNotesBinding.rootFrameLayout, getString(R.string.restored), Snackbar.LENGTH_LONG).show();
            }
        });
        snackbar.setActionTextColor(ViewUtils.getColorFromAttribute(getParentActivity(), R.attr.colorAccent));
        snackbar.show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(BUNDLE_EXTRA, mFilterText);
        super.onSaveInstanceState(outState);
    }

    private FragmentActivity getParentActivity() {
        return getActivity();
    }
}
