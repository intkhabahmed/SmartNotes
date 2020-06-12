package com.intkhabahmed.smartnotes.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.core.widget.CompoundButtonCompat;
import androidx.databinding.DataBindingUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.databinding.ActivityAddAndEditChecklistBinding;
import com.intkhabahmed.smartnotes.models.ChecklistItem;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.services.NoteService;
import com.intkhabahmed.smartnotes.utils.AppExecutors;
import com.intkhabahmed.smartnotes.utils.DateTimeListener;
import com.intkhabahmed.smartnotes.utils.Global;
import com.intkhabahmed.smartnotes.utils.NoteUtils;
import com.intkhabahmed.smartnotes.utils.ReminderUtils;
import com.intkhabahmed.smartnotes.utils.ViewUtils;

import java.util.List;
import java.util.TreeMap;

public class AddAndEditChecklist extends AppCompatActivity implements DateTimeListener {

    private boolean mIsEditing;
    private Note mNote;
    private boolean mIsChanged;
    private int mTrashed;
    private MenuItem menuItem;
    private TreeMap<String, ChecklistItem> mItems;
    private String dateTime;
    private boolean isNotificationEnabled;
    private ActivityAddAndEditChecklistBinding mChecklistBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChecklistBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_and_edit_checklist);

        Toolbar toolbar = mChecklistBinding.toolbar;
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_white_black_24dp);
            actionBar.setTitle(R.string.checklist);
        }

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (mTrashed == 0) {
                    mIsChanged = !TextUtils.isEmpty(mChecklistBinding.checklistItem.getText().toString().trim())
                            || !TextUtils.isEmpty(mChecklistBinding.checklistTitle.getText().toString().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        mChecklistBinding.checklistTitle.addTextChangedListener(textWatcher);

        ImageButton dateTimePickerBtn = findViewById(R.id.date_time_picker_btn);
        dateTimePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.showDatePicker(AddAndEditChecklist.this, AddAndEditChecklist.this);
            }
        });
        mItems = new TreeMap<>();
        mChecklistBinding.addChecklistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String checklistItem = mChecklistBinding.checklistItem.getText().toString().trim().toLowerCase();
                if (TextUtils.isEmpty(checklistItem)) {
                    Toast.makeText(AddAndEditChecklist.this, getString(R.string.empty_item_error),
                            Toast.LENGTH_LONG).show();
                } else if (!mItems.containsKey(checklistItem)) {
                    mChecklistBinding.checklistItem.setText("");
                    mIsChanged = true;
                    if (mItems.size() == 0) {
                        mChecklistBinding.itemsLabelTv.setVisibility(View.VISIBLE);
                        mChecklistBinding.checklistContainer.setVisibility(View.VISIBLE);
                    }
                    addChecklist(checklistItem, false);
                } else {
                    Toast.makeText(AddAndEditChecklist.this, getString(R.string.duplicate_entry_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
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
        CompoundButtonCompat.setButtonTintList(mChecklistBinding.enableNotificationCb, colorStateList);
        mChecklistBinding.enableNotificationCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isNotificationEnabled = isChecked;
                if (isChecked) {
                    mChecklistBinding.notificationGroup.setVisibility(View.VISIBLE);
                } else {
                    mChecklistBinding.notificationGroup.setVisibility(View.GONE);
                }
            }
        });
        Intent intent = getIntent();
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            mNote = intent.getParcelableExtra(Intent.EXTRA_TEXT);
            mIsEditing = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    populateChecklist();
                }
            }, 50);
        } else {
            mIsEditing = false;
        }
    }


    private void addChecklist(final String checklistItem, boolean checked) {
        if (TextUtils.isEmpty(checklistItem)) {
            Toast.makeText(this, getString(R.string.empty_task_error), Toast.LENGTH_LONG).show();
            return;
        }
        final LinearLayout checkBoxContainer = new LinearLayout(this);
        checkBoxContainer.setOrientation(LinearLayout.HORIZONTAL);
        checkBoxContainer.setContentDescription(checklistItem);

        ImageButton removeButton = new ImageButton(this);
        removeButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_clear_24dp));
        removeButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        removeButton.setPadding(0, 10, 0, 5);

        final CheckBox checkBox = new CheckBox(this);
        checkBox.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        checkBox.setText(checklistItem);
        checkBox.setChecked(checked);
        if (checked) {
            checkBox.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        }
        checkBoxContainer.addView(removeButton, 0);
        checkBoxContainer.addView(checkBox, 1);
        if (Build.VERSION.SDK_INT >= 23) {
            checkBox.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
        }
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
        CompoundButtonCompat.setButtonTintList(checkBox, colorStateList);
        checkBox.setTextColor(ViewUtils.getColorFromAttribute(this, R.attr.primaryTextColor));
        removeButton.setColorFilter(ViewUtils.getColorFromAttribute(this, R.attr.iconPlaceHolder));
        mChecklistBinding.checklistContainer.addView(checkBoxContainer);
        mItems.put(checklistItem, new ChecklistItem(checklistItem, checkBox.isChecked()));
        if (mTrashed == 0) {
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        checkBox.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    } else {
                        checkBox.setPaintFlags(0);
                    }
                    mItems.get(checklistItem).setChecked(b);
                }
            });
        } else {
            checkBox.setEnabled(false);
            removeButton.setVisibility(View.GONE);
        }
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTrashed == 0) {
                    mItems.remove(checklistItem);
                    mIsChanged = true;
                    mChecklistBinding.checklistContainer.removeView(checkBoxContainer);
                    if (mItems.size() == 0) {
                        if (!mIsEditing) {
                            mIsChanged = false;
                        }
                        mChecklistBinding.itemsLabelTv.setVisibility(View.GONE);
                        mChecklistBinding.checklistContainer.setVisibility(View.GONE);
                    }
                }
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
        menuItem = menu.findItem(R.id.save_action);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                if (mIsChanged) {
                    onBackPressed();
                } else {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
            case R.id.save_action:
                insertChecklist();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertChecklist() {
        if (mItems.size() < 1) {
            Toast.makeText(this, getString(R.string.empty_task_list_error), Toast.LENGTH_LONG).show();
            return;
        }
        String checklistData = new Gson().toJson(mItems.values());
        String checklistTitle = mChecklistBinding.checklistTitle.getText().toString().trim();
        String dateTimeString = mChecklistBinding.dateTimeTv.getText().toString();

        if (TextUtils.isEmpty(checklistTitle)) {
            Toast.makeText(this, getString(R.string.list_title_error), Toast.LENGTH_LONG).show();
            return;
        }

        final Note note = mIsEditing ? mNote : new Note();
        note.setNoteTitle(checklistTitle);
        note.setDescription(checklistData);
        final int timeToRemind = NoteUtils.getRelativeTimeFromNow(dateTimeString);
        if (timeToRemind < 0 && isNotificationEnabled) {
            Toast.makeText(this, getString(R.string.notification_time_error), Toast.LENGTH_LONG).show();
            return;
        } else if (isNotificationEnabled && timeToRemind == 0) {
            Toast.makeText(this, getString(R.string.notification_error), Toast.LENGTH_LONG).show();
            return;
        } else if (isNotificationEnabled) {
            note.setRemainingTimeToRemind(timeToRemind);
            note.setReminderDateTime(dateTimeString);
        }

        if (!mIsEditing) {
            note.setNoteType(getString(R.string.checklist));
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
                                    ReminderUtils.scheduleNoteReminder(AddAndEditChecklist.this, note);
                                }
                                Toast.makeText(AddAndEditChecklist.this, getString(R.string.note_created_msg), Toast.LENGTH_LONG).show();
                                finish();
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            }
                        }
                    });
                }
            });
            return;
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
                                ReminderUtils.scheduleNoteReminder(AddAndEditChecklist.this, note);
                            }
                            Toast.makeText(AddAndEditChecklist.this, getString(R.string.note_updated_msg), Toast.LENGTH_LONG).show();
                            NoteService.startActionUpdateWidget(AddAndEditChecklist.this);
                            finish();
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }
                    }
                });
            }
        });
    }

    private void populateChecklist() {
        if (mNote != null) {
            mTrashed = mNote.getTrashed();
            if (mTrashed == 1) {
                mChecklistBinding.checklistTitle.setEnabled(false);
                mChecklistBinding.checklistItem.setVisibility(View.GONE);
                mChecklistBinding.addChecklistButton.hide();
                menuItem.setVisible(false);
            }
            mChecklistBinding.checklistTitle.setText(mNote.getNoteTitle());
            if (!TextUtils.isEmpty(mNote.getReminderDateTime()) && NoteUtils.getRelativeTimeFromNow(mNote.getReminderDateTime()) > 0) {
                mChecklistBinding.dateTimeTv.setText(mNote.getReminderDateTime());
                mChecklistBinding.enableNotificationCb.setChecked(true);
            } else {
                mChecklistBinding.dateTimeTv.setText(getString(R.string.notification_desc));
                mChecklistBinding.enableNotificationCb.setChecked(false);
            }
            List<ChecklistItem> checklistItems = new Gson().fromJson(mNote.getDescription(), new TypeToken<List<ChecklistItem>>() {
            }.getType());
            if (checklistItems.size() > 0) {
                mChecklistBinding.itemsLabelTv.setVisibility(View.VISIBLE);
                mChecklistBinding.checklistContainer.setVisibility(View.VISIBLE);
            }
            for (ChecklistItem item : checklistItems) {
                addChecklist(item.getTitle(), item.isChecked());
            }
        }
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
            mChecklistBinding.dateTimeTv.setText(dateTime);
        }
    }
}
