package com.intkhabahmed.smartnotes.ui;

import android.appwidget.AppWidgetManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.adapters.NotesAdapter;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.services.NoteService;
import com.intkhabahmed.smartnotes.utils.AppConstants;
import com.intkhabahmed.smartnotes.utils.Global;
import com.intkhabahmed.smartnotes.utils.ViewUtils;
import com.intkhabahmed.smartnotes.viewmodels.NotesViewModel;
import com.intkhabahmed.smartnotes.viewmodels.NotesViewModelFactory;

import java.util.List;

public class WidgetConfigureActivity extends AppCompatActivity implements NotesAdapter.OnItemClickListener {

    private NotesAdapter mNotesAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayout mEmptyView;
    private ProgressBar mProgressBar;
    private int widgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acivity_widget_configure);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupUi();
        setupViewModel();

    }

    private void setupUi() {
        setResult(RESULT_CANCELED);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_white_black_24dp);
            actionBar.setTitle(R.string.select_note_btn);
        }
        mRecyclerView = findViewById(R.id.recycler_view);
        mEmptyView = findViewById(R.id.empty_view);
        mProgressBar = findViewById(R.id.progress_bar);
        mNotesAdapter = new NotesAdapter(this, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mNotesAdapter);
        mRecyclerView.setHasFixedSize(true);
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                widgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            }
        }
    }

    private void setupViewModel() {
        mProgressBar.setVisibility(View.VISIBLE);
        NotesViewModelFactory factory = new NotesViewModelFactory(null, 0);
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
    public Resources.Theme getTheme() {
        final Resources.Theme theme = super.getTheme();
        boolean isDarkThemeEnabled = Global.getDarkThemeStatus();
        if (isDarkThemeEnabled) {
            theme.applyStyle(R.style.AppThemeDark, true);
        } else {
            theme.applyStyle(R.style.AppThemeLight, true);
        }
        return theme;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMenuItemClick(View view, final Note note) {
        setNoteForWidget(note.getNoteId());
    }

    @Override
    public void onItemClick(int noteId, String noteType) {
        setNoteForWidget(noteId);
    }

    private void setNoteForWidget(int noteId) {
        Global.setDataForWidgetId(AppConstants.PREF + widgetId, noteId);
        NoteService.startActionUpdateWidget(this);
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, intent);
        finish();
    }
}
