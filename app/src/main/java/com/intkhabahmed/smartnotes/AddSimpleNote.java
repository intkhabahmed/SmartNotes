package com.intkhabahmed.smartnotes;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.intkhabahmed.smartnotes.fragments.SimpleNotesDetailFragment;
import com.intkhabahmed.smartnotes.fragments.SimpleNotesFragment;
import com.intkhabahmed.smartnotes.notesdata.NotesContract;

public class AddSimpleNote extends AppCompatActivity {

    private EditText mNoteTitleEditText;
    private EditText mNoteDescriptionEditText;
    private boolean mIsEditing;
    private long mNoteId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_simple_note);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.iconFillColor));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mNoteTitleEditText = findViewById(R.id.note_title_input);
        mNoteDescriptionEditText = findViewById(R.id.note_description_input);

        Intent intent = getIntent();
        if(intent.hasExtra(Intent.EXTRA_TEXT)){
            mIsEditing = true;
            mNoteId = intent.getLongExtra(Intent.EXTRA_TEXT, 0);
            if(mNoteId > 0){
                Cursor cursor = getContentResolver().query(NotesContract.NotesEntry.CONTENT_URI,
                        new String[]{NotesContract.NotesEntry.COLUMN_TITLE, NotesContract.NotesEntry.COLUMN_DESCRIPTION},
                        NotesContract.NotesEntry._ID + "=?", new String[]{String.valueOf(mNoteId)}, null);
                if(cursor != null){
                    cursor.moveToFirst();
                    String title = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_TITLE));
                    String description = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DESCRIPTION));
                    mNoteTitleEditText.setText(title);
                    mNoteDescriptionEditText.setText(description);
                    cursor.close();
                }
            }
        } else {
            mIsEditing = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_note_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.save_action:
                if(mIsEditing){
                    updateSimpleNote();
                }else {
                    insertSimpleNote();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void insertSimpleNote(){
        String noteTitle = mNoteTitleEditText.getText().toString().trim();
        String noteDescription = mNoteDescriptionEditText.getText().toString().trim();

        if(TextUtils.isEmpty(noteTitle) || TextUtils.isEmpty(noteDescription)){
            Toast.makeText(this, "All fields are mandatory", Toast.LENGTH_LONG).show();
            return;
        }
        ContentValues values = new ContentValues();
        values.put(NotesContract.NotesEntry.COLUMN_TITLE, noteTitle);
        values.put(NotesContract.NotesEntry.COLUMN_DESCRIPTION, noteDescription);
        values.put(NotesContract.NotesEntry.COLUMN_TYPE, getString(R.string.simple_note));
        values.put(NotesContract.NotesEntry.COLUMN_DATE_CREATED, System.currentTimeMillis());
        values.put(NotesContract.NotesEntry.COLUMN_DATE_MODIFIED, System.currentTimeMillis());
        Uri uri = getContentResolver().insert(NotesContract.NotesEntry.CONTENT_URI, values);
        if(uri != null){
            Toast.makeText(this, "Note created successfully!", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    public void updateSimpleNote(){
        String noteTitle = mNoteTitleEditText.getText().toString().trim();
        String noteDescription = mNoteDescriptionEditText.getText().toString().trim();

        if(TextUtils.isEmpty(noteTitle) || TextUtils.isEmpty(noteDescription)){
            Toast.makeText(this, "All fields are mandatory", Toast.LENGTH_LONG).show();
            return;
        }
        ContentValues values = new ContentValues();
        values.put(NotesContract.NotesEntry.COLUMN_TITLE, noteTitle);
        values.put(NotesContract.NotesEntry.COLUMN_DESCRIPTION, noteDescription);
        values.put(NotesContract.NotesEntry.COLUMN_DATE_MODIFIED, System.currentTimeMillis());
        int rowUpdated =  getContentResolver().update(NotesContract.NotesEntry.CONTENT_URI, values, NotesContract.NotesEntry._ID + "=?",
                new String[]{String.valueOf(mNoteId)});
        if(rowUpdated > 0){
            Toast.makeText(this, "Note updated successfully!", Toast.LENGTH_LONG).show();
            finish();
        }

    }
}
