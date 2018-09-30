package com.intkhabahmed.smartnotes.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import com.intkhabahmed.smartnotes.notesdata.NotesContract;
import com.intkhabahmed.smartnotes.utils.DBUtils;
import com.intkhabahmed.smartnotes.utils.NoteUtils;
import com.intkhabahmed.smartnotes.utils.ViewUtils;

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
    private FloatingActionButton mAddButton;

    public ChecklistFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notes_recycler_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mEmptyView = view.findViewById(R.id.empty_view);
        mProgressBar = view.findViewById(R.id.progress_bar);
        mNotesAdapter = new NotesAdapter(getActivity(), null, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mNotesAdapter);
        mRecyclerView.setHasFixedSize(true);
        mProgressBar.setVisibility(View.VISIBLE);
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
        getLoaderManager().initLoader(CHECKLIST_FRAGMENT_LOADER_ID, null, ChecklistFragment.this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAddButton.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = NotesContract.NotesEntry.COLUMN_TYPE + "=? AND " + NotesContract.NotesEntry.COLUMN_TRASH + "=?";
        String[] selectionArgs = {getString(R.string.checklist), "0"};
        String sortOrder = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.sort_criteria), NotesContract.NotesEntry.COLUMN_DATE_CREATED + " desc");
        return new CursorLoader(getActivity(), NotesContract.NotesEntry.CONTENT_URI, null,
                selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mProgressBar.setVisibility(View.GONE);
        if (data != null && data.getCount() == 0) {
            ViewUtils.showEmptyView(mRecyclerView, mEmptyView);
        } else {
            ViewUtils.hideEmptyView(mRecyclerView, mEmptyView);
            mNotesAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNotesAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(int adapterPosition, Cursor cursor) {
        cursor.moveToPosition(adapterPosition);
        Intent editChecklistActivityIntent = new Intent(getActivity(), AddAndEditChecklist.class);
        editChecklistActivityIntent.putExtra(Intent.EXTRA_TEXT, cursor.getLong(cursor.getColumnIndex(NotesContract.NotesEntry._ID)));
        startActivity(editChecklistActivityIntent);
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onMenuItemClick(View view, final int noteId) {
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
                                DBUtils.moveToTrash(getActivity(), noteId);
                                showSnackBar(noteId);
                            }
                        };
                        ViewUtils.showDeleteConfirmationDialog(getActivity(), deleteListener);
                        break;
                    case R.id.share_note:
                        Cursor cursor = getActivity().getContentResolver().query(NotesContract.NotesEntry.CONTENT_URI,
                                new String[]{NotesContract.NotesEntry.COLUMN_TITLE, NotesContract.NotesEntry.COLUMN_DESCRIPTION},
                                NotesContract.NotesEntry._ID + "=?", new String[]{String.valueOf(noteId)}, null);
                        if (cursor != null) {
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
                        }
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    public void updateCheckListFragment() {
        getLoaderManager().restartLoader(CHECKLIST_FRAGMENT_LOADER_ID, null, this);
    }

    private void showSnackBar(final int noteId) {
        Snackbar snackbar = Snackbar.make(mAddButton, "Note has been moved to trash", Snackbar.LENGTH_LONG);
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBUtils.restoreFromTrash(getActivity(), noteId);
                Snackbar.make(mAddButton, "Note Restored", Snackbar.LENGTH_LONG).show();
            }
        });
        snackbar.setActionTextColor(ViewUtils.getColorFromAttribute(getActivity(), R.attr.colorAccent));
        snackbar.show();
    }
}

