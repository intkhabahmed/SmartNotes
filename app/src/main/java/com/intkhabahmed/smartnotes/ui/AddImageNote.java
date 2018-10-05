package com.intkhabahmed.smartnotes.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.utils.AppExecutors;
import com.intkhabahmed.smartnotes.utils.BitmapUtils;
import com.intkhabahmed.smartnotes.utils.Global;
import com.intkhabahmed.smartnotes.utils.ViewUtils;

import java.io.File;
import java.io.IOException;

public class AddImageNote extends AppCompatActivity {
    private static final int RC_STORAGE_PERMISSION = 100;
    private static final String FILEPROVIDER_AUTHORITY = "com.intkhabahmed.fileprovider";
    private static final int RC_CAPTURE_IMAGE = 101;
    private ImageButton mCaptureImageButton;
    private EditText mNoteTitleEditText;
    private ImageView mImageView;
    private String mTempImagePath;
    private String mBackupTempImagePath;
    private Bitmap mResultBitmap;
    private Button mChangeImageButton;
    private boolean mIsChanged;
    private Note mNote;
    private boolean mIsEditing;
    private boolean mIsImageChanged;
    private String mOldDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image_note);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_white_black_24dp);
            actionBar.setTitle(R.string.image_note);
        }

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mIsChanged = !TextUtils.isEmpty(mNoteTitleEditText.getText().toString().trim());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        mCaptureImageButton = findViewById(R.id.capture_image_button);
        mNoteTitleEditText = findViewById(R.id.note_title_input);
        mImageView = findViewById(R.id.iv_image_note);
        mChangeImageButton = findViewById(R.id.change_image_button);
        mNoteTitleEditText.addTextChangedListener(textWatcher);

        mChangeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBackupTempImagePath = mTempImagePath;
                checkCameraPermission();
            }
        });

        mCaptureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermission();
            }
        });
        Intent intent = getIntent();
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            mIsEditing = true;
            mNote = intent.getParcelableExtra(Intent.EXTRA_TEXT);
            if (mNote != null) {
                mNoteTitleEditText.setText(mNote.getNoteTitle());
                mOldDescription = mNote.getDescription();
                File image = new File(mNote.getDescription());
                if (image.exists()) {
                    Glide.with(this).asDrawable().load(Uri.fromFile(image)).into(mImageView);
                }
            }
            mChangeImageButton.setVisibility(View.VISIBLE);
            mCaptureImageButton.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);
        } else {
            mIsEditing = false;
            mImageView.setVisibility(View.GONE);
            mChangeImageButton.setVisibility(View.INVISIBLE);
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

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    RC_STORAGE_PERMISSION);
        } else {
            launchCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RC_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera();
                } else {
                    Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_LONG).show();
                }
        }
    }

    private void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photo = null;
            try {
                photo = BitmapUtils.createTempImageFile(this);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            if (photo != null) {
                mTempImagePath = photo.getAbsolutePath();
                Uri photoUri = FileProvider.getUriForFile(this, FILEPROVIDER_AUTHORITY, photo);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(cameraIntent, RC_CAPTURE_IMAGE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            processAndSetImage();
            mIsChanged = true;
        } else {
            if (new File(mTempImagePath).exists()) {
                BitmapUtils.deleteImageFile(this, mTempImagePath);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void processAndSetImage() {
        mImageView.setVisibility(View.VISIBLE);
        mCaptureImageButton.setVisibility(View.GONE);
        mResultBitmap = BitmapUtils.resamplePic(this, mTempImagePath);
        if (mBackupTempImagePath != null && new File(mBackupTempImagePath).exists()) {
            BitmapUtils.deleteImageFile(AddImageNote.this, mBackupTempImagePath);
        }
        mImageView.setImageBitmap(mResultBitmap);
        mChangeImageButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mTempImagePath != null && new File(mTempImagePath).exists()) {
            BitmapUtils.deleteImageFile(this, mTempImagePath);
        }
        if (mBackupTempImagePath != null && new File(mBackupTempImagePath).exists()) {
            BitmapUtils.deleteImageFile(this, mBackupTempImagePath);
        }
        if (mIsImageChanged && !mOldDescription.isEmpty()) {
            BitmapUtils.deleteImageFile(this, mOldDescription);
        }
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
                insertImageNote();
        }
        return super.onOptionsItemSelected(item);
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

    private void insertImageNote() {
        String noteTitle = mNoteTitleEditText.getText().toString().trim();
        if (!noteTitle.matches("[A-Za-z0-9 ]+") || noteTitle.matches("[0-9 ]+")) {
            Toast.makeText(this, "Title can only contain characters or characters and numbers", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(noteTitle)) {
            Toast.makeText(this, "Please enter a title of your note", Toast.LENGTH_LONG).show();
            return;
        }
        if (mResultBitmap == null) {
            Toast.makeText(this, "Please select an image for your note", Toast.LENGTH_LONG).show();
            return;
        }

        final Note note = mIsEditing ? mNote : new Note();
        note.setNoteTitle(noteTitle);
        note.setDescription(saveImageToStorage());

        if (!mIsEditing) {
            note.setNoteType(getString(R.string.image_note));
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
                                Toast.makeText(AddImageNote.this, "Note created successfully!", Toast.LENGTH_LONG).show();
                                finish();
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            }
                        }
                    });
                }
            });
        }
        note.setDateModified(System.currentTimeMillis());
        mIsImageChanged = true;
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final int rowsUpdated = NoteRepository.getInstance().updateNote(note);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (rowsUpdated > 0) {
                            Toast.makeText(AddImageNote.this, "Note updated successfully!", Toast.LENGTH_LONG).show();
                            finish();
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        }
                    }
                });
            }
        });
    }

    public String saveImageToStorage() {
        // Save the image
        return BitmapUtils.saveImage(this, mResultBitmap);
    }
}
