package com.intkhabahmed.smartnotes;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.intkhabahmed.smartnotes.notesdata.NotesContract;
import com.intkhabahmed.smartnotes.utils.BitmapUtils;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image_note);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.iconFillColor));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mCaptureImageButton = findViewById(R.id.capture_image_button);
        mNoteTitleEditText = findViewById(R.id.note_title_input);
        mImageView = findViewById(R.id.iv_image_note);
        mChangeImageButton = findViewById(R.id.change_image_button);

        mChangeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermission();
            }
        });

        mCaptureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermission();
            }
        });
        mImageView.setVisibility(View.GONE);
        mChangeImageButton.setVisibility(View.INVISIBLE);
    }

    private void checkCameraPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
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
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    launchCamera();
                } else {
                    Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_LONG).show();
                }
        }
    }

    private void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraIntent.resolveActivity(getPackageManager()) != null){
            File photo = null;
            try{
                photo = BitmapUtils.createTempImageFile(this);
            } catch (IOException ioe){
                ioe.printStackTrace();
            }

            if(photo != null){
                mTempImagePath = photo.getAbsolutePath();
                Uri photoUri = FileProvider.getUriForFile(this, FILEPROVIDER_AUTHORITY, photo);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(cameraIntent, RC_CAPTURE_IMAGE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RC_CAPTURE_IMAGE && resultCode == RESULT_OK){
            processAndSetImage();
        } else {
            BitmapUtils.deleteImageFile(this, mTempImagePath);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void processAndSetImage() {
        mImageView.setVisibility(View.VISIBLE);
        mCaptureImageButton.setVisibility(View.GONE);
        mResultBitmap = BitmapUtils.resamplePic(this, mTempImagePath);
        if(mBackupTempImagePath != null){
            BitmapUtils.deleteImageFile(AddImageNote.this, mBackupTempImagePath);
        }
        mImageView.setImageBitmap(mResultBitmap);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AddImageNote.this, "Image clicked", Toast.LENGTH_LONG).show();
            }
        });
        mChangeImageButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mTempImagePath != null){
            BitmapUtils.deleteImageFile(this, mTempImagePath);
        }
        if(mBackupTempImagePath != null && (mTempImagePath != null && !mTempImagePath.equals(mBackupTempImagePath)) ){
            BitmapUtils.deleteImageFile(this, mBackupTempImagePath);
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

    private void insertImageNote() {
        String noteTitle = mNoteTitleEditText.getText().toString().trim();

        if(TextUtils.isEmpty(noteTitle)){
            Toast.makeText(this, "Please enter a title of your note", Toast.LENGTH_LONG).show();
            return;
        }
        ContentValues values = new ContentValues();
        values.put(NotesContract.NotesEntry.COLUMN_TITLE, noteTitle);
        values.put(NotesContract.NotesEntry.COLUMN_DESCRIPTION, saveImageToStorage());
        values.put(NotesContract.NotesEntry.COLUMN_TYPE, getString(R.string.image_note));
        values.put(NotesContract.NotesEntry.COLUMN_DATE_CREATED, System.currentTimeMillis());
        values.put(NotesContract.NotesEntry.COLUMN_DATE_MODIFIED, System.currentTimeMillis());
        Uri uri = getContentResolver().insert(NotesContract.NotesEntry.CONTENT_URI, values);
        if(uri != null){
            Toast.makeText(this, "Note created successfully!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public String saveImageToStorage() {
        // Save the image
        return BitmapUtils.saveImage(this, mResultBitmap);
    }
}
