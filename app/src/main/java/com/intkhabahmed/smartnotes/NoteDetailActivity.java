package com.intkhabahmed.smartnotes;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.intkhabahmed.smartnotes.fragments.ImageNotesDetailFragment;
import com.intkhabahmed.smartnotes.fragments.SimpleNotesDetailFragment;
import com.intkhabahmed.smartnotes.notesdata.NotesContract;

public class NoteDetailActivity extends AppCompatActivity {

    private long mNoteId;
    private String mNoteType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.iconFillColor));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        FloatingActionButton editButton = findViewById(R.id.edit_note_button);

        Intent intent = getIntent();
        if(intent.hasExtra(Intent.EXTRA_TEXT) && intent.hasExtra(getString(R.string.note_type))){
            mNoteId = intent.getLongExtra(Intent.EXTRA_TEXT, 0);
            mNoteType = intent.getStringExtra(getString(R.string.note_type));
        }

        if(mNoteType.equals(getString(R.string.simple_note))){
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(NoteDetailActivity.this, AddSimpleNote.class);
                    Uri singleNoteUri = ContentUris.withAppendedId(NotesContract.NotesEntry.CONTENT_URI, mNoteId)
                            .buildUpon().appendPath("0").build();
                    Cursor cursor =  getContentResolver().query(singleNoteUri, null,
                            null, null, null);
                    if(cursor != null){
                        cursor.moveToFirst();
                        long noteId = cursor.getLong(cursor.getColumnIndex(NotesContract.NotesEntry._ID));
                        intent.putExtra(Intent.EXTRA_TEXT, noteId);
                        cursor.close();
                        startActivity(intent);
                    }
                }
            });

            SimpleNotesDetailFragment simpleNotesDetailFragment = new SimpleNotesDetailFragment();
            simpleNotesDetailFragment.setNoteId(mNoteId);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detail_activity_container,simpleNotesDetailFragment)
                    .commit();
        } else if(mNoteType.equals(getString(R.string.image_note))) {
            editButton.setVisibility(View.GONE);
            ImageNotesDetailFragment imageNotesDetailFragment = new ImageNotesDetailFragment();
            imageNotesDetailFragment.setNoteId(mNoteId);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detail_activity_container,imageNotesDetailFragment)
                    .commit();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mNoteType.equals(getString(R.string.simple_note))){
            SimpleNotesDetailFragment simpleNotesDetailFragment = new SimpleNotesDetailFragment();
            simpleNotesDetailFragment.setNoteId(mNoteId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_activity_container,simpleNotesDetailFragment)
                    .commit();
        } else if(mNoteType.equals(getString(R.string.image_note))) {
            ImageNotesDetailFragment imageNotesDetailFragment = new ImageNotesDetailFragment();
            imageNotesDetailFragment.setNoteId(mNoteId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_activity_container,imageNotesDetailFragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}