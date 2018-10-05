package com.intkhabahmed.smartnotes.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.utils.AppExecutors;
import com.intkhabahmed.smartnotes.utils.Global;
import com.intkhabahmed.smartnotes.utils.ViewUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AddAndEditChecklist extends AppCompatActivity {

    public static final String LIST_TITLE = "title";
    public static final String IS_LIST_CHECKED = "isListChecked";
    private EditText mChecklistEditText;
    private LinearLayout mChecklistContainer;
    private JSONArray mChecklistArray;
    private EditText mChecklistTitleEditText;
    private boolean mIsEditing;
    private Note mNote;
    private HashSet<String> mUniqueChecklist;
    private boolean mIsChanged;
    private int mTrashed;
    private ImageButton mAddChecklistItemButton;
    private MenuItem menuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_and_edit_checklist);

        Toolbar toolbar = findViewById(R.id.toolbar);
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
                    mIsChanged = !TextUtils.isEmpty(mChecklistEditText.getText().toString().trim())
                            || !TextUtils.isEmpty(mChecklistTitleEditText.getText().toString().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        mAddChecklistItemButton = findViewById(R.id.add_checklist_button);
        mChecklistEditText = findViewById(R.id.checklist_item);
        mChecklistContainer = findViewById(R.id.checklist_container);
        mChecklistTitleEditText = findViewById(R.id.checklist_title);
        mChecklistTitleEditText.addTextChangedListener(textWatcher);

        mAddChecklistItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String checklistItem = mChecklistEditText.getText().toString().trim().toLowerCase();
                if (TextUtils.isEmpty(checklistItem)) {
                    Toast.makeText(AddAndEditChecklist.this, "Item cannot be empty",
                            Toast.LENGTH_LONG).show();
                } else if (!mUniqueChecklist.contains(checklistItem)) {
                    mUniqueChecklist.add(checklistItem);
                    mChecklistEditText.setText("");
                    mIsChanged = true;
                    addChecklist(checklistItem, false);
                } else {
                    Toast.makeText(AddAndEditChecklist.this, "Duplicate Entry is not allowed",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        mChecklistArray = new JSONArray();
        mUniqueChecklist = new HashSet<>();

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
            Toast.makeText(this, "Task cannot be empty", Toast.LENGTH_LONG).show();
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
        checkBox.setTextColor(ViewUtils.getColorFromAttribute(this, R.attr.primaryTextColor));
        removeButton.setColorFilter(ViewUtils.getColorFromAttribute(this, R.attr.iconPlaceHolder));
        mChecklistContainer.addView(checkBoxContainer);
        final JSONObject checklistObject = new JSONObject();
        try {
            checklistObject.put(LIST_TITLE, checklistItem);
            checklistObject.put(IS_LIST_CHECKED, checkBox.isChecked());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (mTrashed == 0) {
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        checkBox.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    } else {
                        checkBox.setPaintFlags(0);
                    }
                    checklistObject.remove(IS_LIST_CHECKED);
                    mIsChanged = true;
                    try {
                        checklistObject.put(IS_LIST_CHECKED, b);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            checkBox.setEnabled(false);
        }
        mChecklistArray.put(checklistObject);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTrashed == 0) {
                    for (int i = 1; i <= mChecklistContainer.getChildCount(); i++) {
                        View childView = mChecklistContainer.getChildAt(i - 1);
                        if (childView != null) {
                            if (childView.getContentDescription().equals(checklistItem)) {
                                mChecklistArray = remove(i - 1, mChecklistArray);
                            }
                        }
                    }
                    mUniqueChecklist.remove(checklistItem);
                    mIsChanged = true;
                    mChecklistContainer.removeView(checkBoxContainer);
                    if (mUniqueChecklist.size() == 0 && !mIsEditing) {
                        mIsChanged = false;
                    }
                }
            }
        });
        if (mTrashed == 1) {
            removeButton.setVisibility(View.GONE);
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
        menuItem = menu.findItem(R.id.save_action);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.save_action:
                insertChecklist();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void insertChecklist() {
        if (mChecklistArray.length() < 1) {
            Toast.makeText(this, "You need to add at least one task", Toast.LENGTH_LONG).show();
            return;
        }
        JSONObject object = new JSONObject();
        try {
            object.put(getString(R.string.checklist), mChecklistArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String checklistData = object.toString();
        String checklistTitle = mChecklistTitleEditText.getText().toString().trim();

        if (TextUtils.isEmpty(checklistTitle)) {
            Toast.makeText(this, "You should give a title to your list", Toast.LENGTH_LONG).show();
            return;
        }

        final Note note = mIsEditing ? mNote : new Note();
        note.setNoteTitle(checklistTitle);
        note.setDescription(checklistData);

        if (!mIsEditing) {
            note.setNoteType(getString(R.string.checklist));
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
                                Toast.makeText(AddAndEditChecklist.this, "Note created successfully!", Toast.LENGTH_LONG).show();
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
                            Toast.makeText(AddAndEditChecklist.this, "Note updated successfully!", Toast.LENGTH_LONG).show();
                            finish();
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }
                    }
                });
            }
        });
        mChecklistArray = null;
        mUniqueChecklist = null;
    }

    public static JSONArray remove(final int idx, final JSONArray from) {
        final List<JSONObject> objs = asList(from);
        objs.remove(idx);

        final JSONArray ja = new JSONArray();
        for (final JSONObject obj : objs) {
            ja.put(obj);
        }
        return ja;
    }

    public static List<JSONObject> asList(final JSONArray ja) {
        final int len = ja.length();
        final ArrayList<JSONObject> result = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            final JSONObject obj = ja.optJSONObject(i);
            if (obj != null) {
                result.add(obj);
            }
        }
        return result;
    }

    public void populateChecklist() {
        if (mNote != null) {
            mTrashed = mNote.getTrashed();
            if (mTrashed == 1) {
                mChecklistTitleEditText.setEnabled(false);
                mChecklistEditText.setVisibility(View.GONE);
                mAddChecklistItemButton.setVisibility(View.GONE);
                menuItem.setVisible(false);
            }
            String title = mNote.getNoteTitle();
            mChecklistTitleEditText.setText(title);
            String description = mNote.getDescription();

            try {
                JSONObject checklistObjects = new JSONObject(description);
                JSONArray jsonArrays = checklistObjects.getJSONArray(getString(R.string.checklist));
                for (int i = 0; i < jsonArrays.length(); i++) {
                    try {
                        JSONObject jsonObject = jsonArrays.getJSONObject(i);
                        String task = String.valueOf(jsonObject.get(AddAndEditChecklist.LIST_TITLE));
                        mUniqueChecklist.add(task);
                        boolean isCompleted = jsonObject.getBoolean(AddAndEditChecklist.IS_LIST_CHECKED);
                        addChecklist(task, isCompleted);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
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
}
