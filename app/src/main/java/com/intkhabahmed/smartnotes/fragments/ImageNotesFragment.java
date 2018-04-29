package com.intkhabahmed.smartnotes.fragments;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.intkhabahmed.smartnotes.NoteDetailActivity;
import com.intkhabahmed.smartnotes.NotesAdapter;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.notesdata.NotesContract;
import com.intkhabahmed.smartnotes.utils.BitmapUtils;

public class ImageNotesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, NotesAdapter.OnItemClickListener {
    private NotesAdapter mNotesAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayout mEmptyView;
    private ProgressBar mProgressBar;
    private static final int IMAGE_NOTE_FRAGMENT_LOADER_ID = 2;

    public ImageNotesFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.notes_recycler_view, container, false);
        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        mEmptyView = rootView.findViewById(R.id.empty_view);
        mProgressBar = rootView.findViewById(R.id.progress_bar);

        mNotesAdapter = new NotesAdapter(getActivity(), null, this, true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(mNotesAdapter);
        mRecyclerView.setHasFixedSize(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mProgressBar.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.INVISIBLE);
        getLoaderManager().initLoader(IMAGE_NOTE_FRAGMENT_LOADER_ID, null, ImageNotesFragment.this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = NotesContract.NotesEntry.COLUMN_TYPE + "=? AND " + NotesContract.NotesEntry.COLUMN_TRASH + "=?";
        String[] selectionArgs = {getString(R.string.image_note), "0"};
        String sortOrder = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.sort_criteria), NotesContract.NotesEntry.COLUMN_DATE_CREATED + " desc");
        return new CursorLoader(getActivity(), NotesContract.NotesEntry.CONTENT_URI, null,
                selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mProgressBar.setVisibility(View.GONE);
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
        mEmptyView.setVisibility(View.VISIBLE);
    }

    private void hideEmptyView() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.INVISIBLE);
    }


    public void updateImageNotesFragment() {
        getLoaderManager().restartLoader(IMAGE_NOTE_FRAGMENT_LOADER_ID, null, this);
    }

    @Override
    public void onItemClick(int adapterPosition, Cursor cursor) {
        cursor.moveToPosition(adapterPosition);
        Intent detailActivityIntent = new Intent(getActivity(), NoteDetailActivity.class);
        detailActivityIntent.putExtra(Intent.EXTRA_TEXT, cursor.getLong(cursor.getColumnIndex(NotesContract.NotesEntry._ID)));
        detailActivityIntent.putExtra(getString(R.string.note_type), getString(R.string.image_note));
        startActivity(detailActivityIntent);
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
                        getLoaderManager().restartLoader(IMAGE_NOTE_FRAGMENT_LOADER_ID, null, ImageNotesFragment.this);
                        break;
                    case R.id.share_note:
                        Cursor cursor = getActivity().getContentResolver().query(NotesContract.NotesEntry.CONTENT_URI,
                                new String[]{NotesContract.NotesEntry.COLUMN_DESCRIPTION},
                                NotesContract.NotesEntry._ID + "=?", new String[]{String.valueOf(noteId)}, null);
                        if (cursor != null) {
                            cursor.moveToFirst();
                            String imagePath = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DESCRIPTION));
                            cursor.close();
                            BitmapUtils.shareImage(getActivity(), imagePath);
                        }
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }
}
