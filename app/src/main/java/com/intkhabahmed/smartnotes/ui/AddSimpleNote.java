package com.intkhabahmed.smartnotes.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.utils.AppExecutors;
import com.intkhabahmed.smartnotes.utils.Global;
import com.intkhabahmed.smartnotes.utils.ViewUtils;

public class AddSimpleNote extends AppCompatActivity {

    private EditText mNoteTitleEditText;
    private EditText mNoteDescriptionEditText;
    private boolean mIsEditing;
    private Note mNote;
    private boolean mIsChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_simple_note);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_white_black_24dp);
            actionBar.setTitle(R.string.simple_note);
        }

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mIsChanged = !TextUtils.isEmpty(mNoteTitleEditText.getText().toString().trim())
                        || !TextUtils.isEmpty(mNoteDescriptionEditText.getText().toString().trim());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        mNoteTitleEditText = findViewById(R.id.note_title_input);
        mNoteDescriptionEditText = findViewById(R.id.note_description_input);
        mNoteTitleEditText.addTextChangedListener(textWatcher);
        mNoteDescriptionEditText.addTextChangedListener(textWatcher);

        Intent intent = getIntent();
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            mIsEditing = true;
            mNote = intent.getParcelableExtra(Intent.EXTRA_TEXT);
            if (mNote != null) {
                mNoteTitleEditText.setText(mNote.getNoteTitle());
                mNoteDescriptionEditText.setText(mNote.getDescription());
            }
        } else {
            mIsEditing = false;
        }
    }

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
        boolean isDarkThemeEnabled = Global.getDarkThemeStatus();
        if (isDarkThemeEnabled) {
            theme.applyStyle(R.style.AppThemeDark, true);
        } else {
            theme.applyStyle(R.style.AppThemeLight, true);
        }
        return theme;
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
                return true;
            case R.id.save_action:
                insertSimpleNote();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void insertSimpleNote() {
        String noteTitle = mNoteTitleEditText.getText().toString().trim();
        String noteDescription = mNoteDescriptionEditText.getText().toString().trim();

        if (TextUtils.isEmpty(noteTitle) || TextUtils.isEmpty(noteDescription)) {
            Toast.makeText(this, getString(R.string.mandatory_fields_error), Toast.LENGTH_LONG).show();
            return;
        }
        final Note note = mIsEditing ? mNote : new Note();
        note.setNoteTitle(noteTitle);
        note.setDescription(noteDescription);

        if (!mIsEditing) {
            note.setNoteType(getString(R.string.simple_note));
            note.setDateCreated(System.currentTimeMillis());
            note.setDateModified(0);
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    final long noteId = NoteRepository.getInstance().insertNote(note);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (noteId > 0) {
                                Toast.makeText(AddSimpleNote.this, getString(R.string.note_created_msg), Toast.LENGTH_LONG).show();
                                finish();
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            }
                        }
                    });
                }
            });
        }
        note.setDateModified(System.currentTimeMillis());
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final int rowsUpdated = NoteRepository.getInstance().updateNote(note);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (rowsUpdated > 0) {
                            Toast.makeText(AddSimpleNote.this, getString(R.string.note_updated_msg), Toast.LENGTH_LONG).show();
                            finish();
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mIsChanged) {
            ViewUtils.showUnsavedChangesDialog(this);
        } else {
            super.onBackPressed();
        }
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
