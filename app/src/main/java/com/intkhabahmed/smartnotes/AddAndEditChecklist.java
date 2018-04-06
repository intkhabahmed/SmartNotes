package com.intkhabahmed.smartnotes;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.intkhabahmed.smartnotes.notesdata.NotesContract;

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
    private long mNoteId;
    private HashSet<String> mUniqueChecklist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_and_edit_checklist);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.iconFillColor));
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar_layout);
        collapsingToolbarLayout.setTitle(getString(R.string.checklist));
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.iconFillColor));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ImageButton addChecklistItemButton = findViewById(R.id.add_checklist_button);
        mChecklistEditText = findViewById(R.id.checklist_item);
        mChecklistContainer = findViewById(R.id.checklist_container);

        addChecklistItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String checklistItem = mChecklistEditText.getText().toString().trim().toLowerCase();
                if(!mUniqueChecklist.contains(checklistItem)){
                    mUniqueChecklist.add(checklistItem);
                    mChecklistEditText.setText("");
                    addChecklist(checklistItem, false);
                } else {
                    Toast.makeText(AddAndEditChecklist.this, "Duplicate Entry is not allowed",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        mChecklistTitleEditText = findViewById(R.id.checklist_title);

        mChecklistArray = new JSONArray();
        mUniqueChecklist = new HashSet<>();

        Intent intent = getIntent();
        if(intent.hasExtra(Intent.EXTRA_TEXT)){
            mNoteId = intent.getLongExtra(Intent.EXTRA_TEXT, 0);
            mIsEditing = true;
            populateChecklist();
        } else {
            mIsEditing = false;
        }
    }

    private void addChecklist(final String checklistItem, boolean checked) {
        if(TextUtils.isEmpty(checklistItem)){
            Toast.makeText(this, "Task cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }
        final LinearLayout checkBoxContainer = new LinearLayout(this);
        checkBoxContainer.setOrientation(LinearLayout.HORIZONTAL);
        checkBoxContainer.setContentDescription(checklistItem);

        ImageButton removeButton = new ImageButton(this);
        removeButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_clear_black_24dp));
        removeButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

        final CheckBox checkBox = new CheckBox(this);
        checkBox.setText(checklistItem);
        checkBox.setChecked(checked);
        checkBoxContainer.addView(removeButton, 0);
        checkBoxContainer.addView(checkBox, 1);
        if(Build.VERSION.SDK_INT >= 23){
            checkBox.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
        }
        mChecklistContainer.addView(checkBoxContainer);
        final JSONObject checklistObject = new JSONObject();
        try {
            checklistObject.put(LIST_TITLE, checklistItem);
            checklistObject.put(IS_LIST_CHECKED, checkBox.isChecked());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checklistObject.remove(IS_LIST_CHECKED);
                try {
                    checklistObject.put(IS_LIST_CHECKED,b);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        mChecklistArray.put(checklistObject);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i=1; i <= mChecklistContainer.getChildCount();i++){
                    View childView = mChecklistContainer.getChildAt(i-1);
                    if(childView != null) {
                        if(childView.getContentDescription().equals(checklistItem)){
                            mChecklistArray = remove(i-1, mChecklistArray);
                        }
                    }
                }
                mChecklistContainer.removeView(checkBoxContainer);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_note_menu, menu);
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

    public void insertChecklist(){
        if(mChecklistArray.length() < 1){
            Toast.makeText(this, "You need to add atleat one task", Toast.LENGTH_LONG).show();
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

        if(TextUtils.isEmpty(checklistTitle)){
            Toast.makeText(this, "You should give a title to your list", Toast.LENGTH_LONG).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(NotesContract.NotesEntry.COLUMN_TITLE, checklistTitle);
        values.put(NotesContract.NotesEntry.COLUMN_DESCRIPTION, checklistData);

        if(!mIsEditing){
            values.put(NotesContract.NotesEntry.COLUMN_TYPE, getString(R.string.checklist));
            values.put(NotesContract.NotesEntry.COLUMN_DATE_CREATED, System.currentTimeMillis());
            values.put(NotesContract.NotesEntry.COLUMN_DATE_MODIFIED, System.currentTimeMillis());
            Uri uri = getContentResolver().insert(NotesContract.NotesEntry.CONTENT_URI, values);
            if(uri != null){
                Toast.makeText(this, "Note created successfully!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        values.put(NotesContract.NotesEntry.COLUMN_DATE_MODIFIED, System.currentTimeMillis());
        int rowsUpdated = getContentResolver().update(NotesContract.NotesEntry.CONTENT_URI, values,
                NotesContract.NotesEntry._ID + "=?", new String[]{String.valueOf(mNoteId)});
        if(rowsUpdated > 0){
            Toast.makeText(this, "Note updated successfully!", Toast.LENGTH_LONG).show();
            finish();
        }
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

    public void populateChecklist(){
        Cursor cursor = getContentResolver().query(NotesContract.NotesEntry.CONTENT_URI,
                new String[]{NotesContract.NotesEntry.COLUMN_TITLE, NotesContract.NotesEntry.COLUMN_DESCRIPTION},
                NotesContract.NotesEntry._ID + "=?", new String[]{String.valueOf(mNoteId)}, null);
        if(cursor != null){
            cursor.moveToFirst();
            String title = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_TITLE));
            mChecklistTitleEditText.setText(title);
            String description = cursor.getString(cursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DESCRIPTION));
            try {
                JSONObject checklistObjects = new JSONObject(description);
                JSONArray jsonArrays = checklistObjects.getJSONArray(getString(R.string.checklist));
                for(int i=0;i<jsonArrays.length();i++){
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
            cursor.close();
        }
    }
}
