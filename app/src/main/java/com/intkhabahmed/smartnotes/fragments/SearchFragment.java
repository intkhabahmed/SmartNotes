package com.intkhabahmed.smartnotes.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.intkhabahmed.smartnotes.AddAndEditChecklist;
import com.intkhabahmed.smartnotes.NoteDetailActivity;
import com.intkhabahmed.smartnotes.NotesAdapter;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.notesdata.NotesContract;

public class SearchFragment extends Fragment implements NotesAdapter.OnItemClickListener, SearchView.OnQueryTextListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int SEARCH_NOTE_FRAGMENT_LOADER_ID = 4;
    private NotesAdapter mNotesAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayout mEmptyView;
    private SearchView mSearchView;
    private String mFilterText;

    public SearchFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notes_recycler_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mEmptyView = view.findViewById(R.id.search_error_view);

        mNotesAdapter = new NotesAdapter(getActivity(), null, this, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mNotesAdapter);
        mRecyclerView.setHasFixedSize(true);
        getLoaderManager().initLoader(SEARCH_NOTE_FRAGMENT_LOADER_ID, null, SearchFragment.this);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.search_menu, menu);
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
        searchViewItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                mSearchView.setQuery("", false);
                getActivity().getSupportFragmentManager().popBackStack(HomePageFragment.class.getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
        getLoaderManager().restartLoader(SEARCH_NOTE_FRAGMENT_LOADER_ID, null, this);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri contentUri = NotesContract.NotesEntry.CONTENT_URI.buildUpon().appendPath("0").appendPath(mFilterText).build();
        String sortOrder = NotesContract.NotesEntry.COLUMN_TITLE;
        return new CursorLoader(getActivity(), contentUri, null,
                null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.getCount() == 0) {
            showEmptyView();
        } else {
            hideEmptyView();
            mNotesAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNotesAdapter.swapCursor(null);
    }

    private void showEmptyView() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        if(!TextUtils.isEmpty(mFilterText)) {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    private void hideEmptyView() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onMenuItemClick(View view, final long noteId) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        popupMenu.inflate(R.menu.item_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.delete_note:
                        ContentValues values = new ContentValues();
                        values.put(NotesContract.NotesEntry.COLUMN_TRASH, 1);
                        values.put(NotesContract.NotesEntry.COLUMN_DATE_MODIFIED, System.currentTimeMillis());
                        getActivity().getContentResolver().update(NotesContract.NotesEntry.CONTENT_URI, values,
                                NotesContract.NotesEntry._ID + "=?", new String[]{String.valueOf(noteId)});
                        Toast.makeText(getActivity(), "Note has been moved to trash ", Toast.LENGTH_LONG).show();
                        getLoaderManager().restartLoader(SEARCH_NOTE_FRAGMENT_LOADER_ID, null, SearchFragment.this);
                        break;
                    case R.id.share_note:
                        Cursor cursor = getActivity().getContentResolver().query(NotesContract.NotesEntry.CONTENT_URI, new String[]{NotesContract.NotesEntry.COLUMN_DESCRIPTION},
                                NotesContract.NotesEntry._ID + "=?", new String[]{String.valueOf(noteId)}, null);
                        if(cursor != null) {
                            cursor.moveToFirst();
                            String noteDescription = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DESCRIPTION));
                            cursor.close();
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_TEXT, noteDescription);
                            if (shareIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivity(shareIntent);
                            }
                        }
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onItemClick(int adapterPosition, Cursor cursor) {
        cursor.moveToPosition(adapterPosition);
        String noteType = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_TYPE));
        Intent detailActivityIntent;
        if(noteType.equals(getString(R.string.checklist))){
            detailActivityIntent = new Intent(getActivity(), AddAndEditChecklist.class);
        } else {
            detailActivityIntent = new Intent(getActivity(), NoteDetailActivity.class);
            detailActivityIntent.putExtra(getString(R.string.note_type), noteType);
        }
        detailActivityIntent.putExtra(Intent.EXTRA_TEXT, cursor.getLong(cursor.getColumnIndex(NotesContract.NotesEntry._ID)));
        startActivity(detailActivityIntent);
    }
}
