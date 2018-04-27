package com.intkhabahmed.smartnotes.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.intkhabahmed.smartnotes.AddAndEditChecklist;
import com.intkhabahmed.smartnotes.NotesAdapter;
import com.intkhabahmed.smartnotes.notesdata.NotesContract;
import com.intkhabahmed.smartnotes.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by INTKHAB on 23-03-2018.
 */

public class ChecklistFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, NotesAdapter.OnItemClickListener {

    private static final int CHECKLIST_FRAGMENT_LOADER_ID = 1;
    private NotesAdapter mNotesAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayout mEmptyView;
    private ProgressBar mProgressBar;
    private String mFilterText;

    public ChecklistFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.notes_recycler_view, container, false);
        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        mEmptyView = rootView.findViewById(R.id.empty_view);
        mProgressBar = rootView.findViewById(R.id.progress_bar);

        mNotesAdapter = new NotesAdapter(getActivity(),null, this, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,  false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mNotesAdapter);
        mRecyclerView.setHasFixedSize(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mProgressBar.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.INVISIBLE);
        getLoaderManager().initLoader(CHECKLIST_FRAGMENT_LOADER_ID, null, ChecklistFragment.this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri contentUri = TextUtils.isEmpty(mFilterText) ? NotesContract.NotesEntry.CONTENT_URI
                : NotesContract.NotesEntry.CONTENT_URI.buildUpon().appendPath(mFilterText).build();
        String selection = NotesContract.NotesEntry.COLUMN_TYPE +"=? AND " + NotesContract.NotesEntry.COLUMN_TRASH + "=?";
        String[] selectionArgs = {getString(R.string.checklist), "0"};
        String sortOrder = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.sort_criteria), NotesContract.NotesEntry.COLUMN_DATE_CREATED + " desc");
        return new CursorLoader(getActivity(), contentUri, null,
                selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mProgressBar.setVisibility(View.GONE);
        if(data != null && data.getCount()==0){
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

    private void showEmptyView(){
        mRecyclerView.setVisibility(View.INVISIBLE);
        mEmptyView.setVisibility(View.VISIBLE);
    }

    private void hideEmptyView(){
        mRecyclerView.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onItemClick(int adapterPosition, Cursor cursor) {
        cursor.moveToPosition(adapterPosition);
        Intent editChecklistActivityIntent = new Intent(getActivity(), AddAndEditChecklist.class);
        editChecklistActivityIntent.putExtra(Intent.EXTRA_TEXT, cursor.getLong(cursor.getColumnIndex(NotesContract.NotesEntry._ID)));
        startActivity(editChecklistActivityIntent);
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

                        getActivity().getContentResolver().update(NotesContract.NotesEntry.CONTENT_URI, values,
                                NotesContract.NotesEntry._ID + "=?", new String[]{String.valueOf(noteId)});
                        Toast.makeText(getActivity(), "Note has been moved to trash ", Toast.LENGTH_LONG).show();
                        getLoaderManager().restartLoader(CHECKLIST_FRAGMENT_LOADER_ID, null, ChecklistFragment.this);
                        break;
                    case R.id.share_note:
                        Cursor cursor = getActivity().getContentResolver().query(NotesContract.NotesEntry.CONTENT_URI,
                                new String[]{NotesContract.NotesEntry.COLUMN_TITLE, NotesContract.NotesEntry.COLUMN_DESCRIPTION},
                                NotesContract.NotesEntry._ID + "=?", new String[]{String.valueOf(noteId)}, null);
                        if(cursor != null) {
                            cursor.moveToFirst();
                            String noteTitle = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_TITLE));
                            String noteDescription = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DESCRIPTION));
                            cursor.close();
                            StringBuilder tasks = new StringBuilder();
                            tasks.append(noteTitle);
                            tasks.append("\n_____________________");
                            try {
                                JSONObject checklistObjects = new JSONObject(noteDescription);
                                JSONArray jsonArrays = checklistObjects.getJSONArray(getActivity().getString(R.string.checklist));
                                for(int i=0;i<jsonArrays.length();i++){
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
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_TEXT, tasks.toString());
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

    public void updateCheckListFragment(String filterText) {
        mFilterText = filterText;
        getLoaderManager().restartLoader(CHECKLIST_FRAGMENT_LOADER_ID, null, this);
    }
}

