package com.intkhabahmed.smartnotes.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.constraint.Group;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.utils.AppExecutors;
import com.intkhabahmed.smartnotes.utils.DateTimeListener;
import com.intkhabahmed.smartnotes.utils.Global;
import com.intkhabahmed.smartnotes.utils.NoteUtils;
import com.intkhabahmed.smartnotes.utils.ReminderUtils;
import com.intkhabahmed.smartnotes.utils.ViewUtils;

public class AddSimpleNote extends AppCompatActivity implements DateTimeListener {

    private EditText mNoteTitleEditText;
    private EditText mNoteDescriptionEditText;
    private boolean mIsEditing;
    private Note mNote;
    private boolean mIsChanged;
    private String dateTime;
    private TextView dateTimeTv;
    private Group notificationGroup;
    private boolean isNotificationEnabled;

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
        notificationGroup = findViewById(R.id.notification_group);
        mNoteDescriptionEditText = findViewById(R.id.note_description_input);
        dateTimeTv = findViewById(R.id.date_time_tv);
        mNoteTitleEditText.addTextChangedListener(textWatcher);
        mNoteDescriptionEditText.addTextChangedListener(textWatcher);
        CheckBox notificationCb = findViewById(R.id.enable_notification_cb);
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked}, // unchecked
                        new int[]{android.R.attr.state_checked}, // checked
                },
                new int[]{
                        ViewUtils.getColorFromAttribute(this, R.attr.primaryTextColor),
                        ViewUtils.getColorFromAttribute(this, R.attr.colorAccent),
                }
        );
        CompoundButtonCompat.setButtonTintList(notificationCb, colorStateList);
        notificationCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isNotificationEnabled = isChecked;
                if (isChecked) {
                    notificationGroup.setVisibility(View.VISIBLE);
                } else {
                    notificationGroup.setVisibility(View.GONE);
                }
            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            mIsEditing = true;
            mNote = intent.getParcelableExtra(Intent.EXTRA_TEXT);
            if (mNote != null) {
                mNoteTitleEditText.setText(mNote.getNoteTitle());
                mNoteDescriptionEditText.setText(mNote.getDescription());
                if (!TextUtils.isEmpty(mNote.getReminderDateTime()) && NoteUtils.getRelativeTimeFromNow(mNote.getReminderDateTime()) > 0) {
                    dateTimeTv.setText(mNote.getReminderDateTime());
                    notificationCb.setChecked(true);
                } else {
                    dateTimeTv.setText(getString(R.string.notification_desc));
                    notificationCb.setChecked(false);
                }
            }
        } else {
            mIsEditing = false;
        }

        ImageButton dateTimePickerBtn = findViewById(R.id.date_time_picker_btn);
        dateTimePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.showDatePicker(AddSimpleNote.this, AddSimpleNote.this);
            }
        });
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
        String dateTimeString = dateTimeTv.getText().toString();

        if (TextUtils.isEmpty(noteTitle) || TextUtils.isEmpty(noteDescription)) {
            Toast.makeText(this, getString(R.string.mandatory_fields_error), Toast.LENGTH_LONG).show();
            return;
        }
        final Note note = mIsEditing ? mNote : new Note();
        note.setNoteTitle(noteTitle);
        note.setDescription(noteDescription);
        final int timeToRemind = NoteUtils.getRelativeTimeFromNow(dateTimeString);
        if (timeToRemind < 0 && isNotificationEnabled) {
            Toast.makeText(this, getString(R.string.notification_time_error), Toast.LENGTH_LONG).show();
            return;
        } else if (isNotificationEnabled && timeToRemind > 0) {
            note.setRemainingTimeToRemind(timeToRemind);
            note.setReminderDateTime(dateTimeString);
        }

        if (!mIsEditing) {
            note.setNoteType(getString(R.string.simple_note));
            note.setDateCreated(System.currentTimeMillis());
            note.setDateModified(0);
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    note.setNoteId((int) NoteRepository.getInstance().insertNote(note));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (note.getNoteId() > 0) {
                                if (timeToRemind > 0) {
                                    ReminderUtils.scheduleNoteReminder(AddSimpleNote.this, note);
                                }
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
                            if (timeToRemind > 0) {
                                ReminderUtils.scheduleNoteReminder(AddSimpleNote.this, note);
                            }
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

    @Override
    public void selectedDate(String date) {
        dateTime = date;
    }

    @Override
    public void selectedTime(String time) {
        dateTime += " " + time;
    }

    @Override
    public void dateTimeSelected(boolean isSelected) {
        if (isSelected) {
            dateTimeTv.setText(dateTime);
        }
    }
}
