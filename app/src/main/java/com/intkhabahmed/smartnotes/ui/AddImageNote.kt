package com.intkhabahmed.smartnotes.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources.Theme
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.CompoundButtonCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.intkhabahmed.smartnotes.R
import com.intkhabahmed.smartnotes.database.NoteRepository
import com.intkhabahmed.smartnotes.databinding.ActivityAddImageNoteBinding
import com.intkhabahmed.smartnotes.models.Note
import com.intkhabahmed.smartnotes.services.NoteService
import com.intkhabahmed.smartnotes.utils.AppExecutors
import com.intkhabahmed.smartnotes.utils.BitmapUtils
import com.intkhabahmed.smartnotes.utils.DateTimeListener
import com.intkhabahmed.smartnotes.utils.Global
import com.intkhabahmed.smartnotes.utils.NoteUtils.getRelativeTimeFromNow
import com.intkhabahmed.smartnotes.utils.ReminderUtils
import com.intkhabahmed.smartnotes.utils.ViewUtils
import kotlinx.android.synthetic.main.activity_add_image_note.toolbar
import java.io.File
import java.io.IOException

class AddImageNote() : AppCompatActivity(), DateTimeListener {
    private var mTempImagePath: String? = null
    private var mBackupTempImagePath: String? = null
    private var mResultBitmap: Bitmap? = null
    private var mIsChanged = false
    private lateinit var mNote: Note
    private var mIsEditing = false
    private var mIsImageChanged = false
    private var mOldDescription: String? = null
    private var dateTime: String? = null
    private var isNotificationEnabled = false
    private lateinit var mImageBinding: ActivityAddImageNoteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mImageBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_image_note)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_arrow_white_black_24dp)
            it.setTitle(R.string.image_note)
        }
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                mIsChanged = !TextUtils.isEmpty(mImageBinding.noteTitleInput.text.toString().trim { it <= ' ' })
            }

            override fun afterTextChanged(editable: Editable) {}
        }
        mImageBinding.noteTitleInput.addTextChangedListener(textWatcher)
        val notificationCb = findViewById<CheckBox>(R.id.enable_notification_cb)
        val colorStateList = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)), intArrayOf(
                ViewUtils.getColorFromAttribute(this, R.attr.primaryTextColor),
                ViewUtils.getColorFromAttribute(this, R.attr.colorAccent)))
        CompoundButtonCompat.setButtonTintList(notificationCb, colorStateList)
        notificationCb.setOnCheckedChangeListener { _, isChecked ->
            isNotificationEnabled = isChecked
            if (isChecked) {
                mImageBinding.notificationGroup.visibility = View.VISIBLE
            } else {
                mImageBinding.notificationGroup.visibility = View.GONE
            }
        }
        val dateTimePickerBtn = findViewById<ImageButton>(R.id.date_time_picker_btn)
        dateTimePickerBtn.setOnClickListener { ViewUtils.showDatePicker(this, this) }
        mImageBinding.changeImageButton.setOnClickListener {
            mBackupTempImagePath = mTempImagePath
            checkCameraPermission()
        }
        mImageBinding.captureImageButton.setOnClickListener { checkCameraPermission() }
        val intent = intent
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            mIsEditing = true
            mNote = intent.getParcelableExtra(Intent.EXTRA_TEXT) as Note
            mImageBinding.noteTitleInput.setText(mNote.noteTitle)
            mOldDescription = mNote.description
            if (!TextUtils.isEmpty(mNote.reminderDateTime) && getRelativeTimeFromNow(mNote.reminderDateTime) > 0) {
                mImageBinding.dateTimeTv.text = mNote.reminderDateTime
                notificationCb.isChecked = true
            } else {
                mImageBinding.dateTimeTv.text = getString(R.string.notification_desc)
                notificationCb.isChecked = false
            }
            val image = File(mNote.description ?: "")
            if (image.exists()) {
                Glide.with(this).asDrawable().load(Uri.fromFile(image)).into(mImageBinding.ivImageNote)
                Glide.with(this).asBitmap().load(Uri.fromFile(image)).into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                        mResultBitmap = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {

                    }
                })
            }
            mImageBinding.changeImageButton.visibility = View.VISIBLE
            mImageBinding.captureImageButton.visibility = View.GONE
            mImageBinding.ivImageNote.visibility = View.VISIBLE
        } else {
            mIsEditing = false
            mImageBinding.ivImageNote.visibility = View.GONE
            mImageBinding.changeImageButton.visibility = View.INVISIBLE
        }
    }

    override fun getTheme(): Theme {
        val theme = super.getTheme()
        val isDarkThemeEnabled = Global.getDarkThemeStatus()
        if (isDarkThemeEnabled) {
            theme.applyStyle(R.style.AppThemeDark, true)
        } else {
            theme.applyStyle(R.style.AppThemeLight, true)
        }
        return theme
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    RC_STORAGE_PERMISSION)
        } else {
            launchCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            RC_STORAGE_PERMISSION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                Toast.makeText(this, getString(R.string.storage_permission_error), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun launchCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            var photo: File? = null
            try {
                photo = BitmapUtils.createTempImageFile(this)
            } catch (ioe: IOException) {
                ioe.printStackTrace()
            }
            if (photo != null) {
                mTempImagePath = photo.absolutePath
                val photoUri = FileProvider.getUriForFile(this, FILEPROVIDER_AUTHORITY, photo)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(cameraIntent, RC_CAPTURE_IMAGE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_CAPTURE_IMAGE && resultCode == Activity.RESULT_OK) {
            processAndSetImage()
            mIsChanged = true
        } else {
            if (File(mTempImagePath!!).exists()) {
                BitmapUtils.deleteImageFile(this, mTempImagePath)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun processAndSetImage() {
        mImageBinding.ivImageNote.visibility = View.VISIBLE
        mImageBinding.captureImageButton.visibility = View.GONE
        mResultBitmap = BitmapUtils.resamplePic(this, mTempImagePath)
        if (mBackupTempImagePath != null && File(mBackupTempImagePath!!).exists()) {
            BitmapUtils.deleteImageFile(this@AddImageNote, mBackupTempImagePath)
        }
        mImageBinding.ivImageNote.setImageBitmap(mResultBitmap)
        mImageBinding.changeImageButton.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        if (mTempImagePath != null && File(mTempImagePath!!).exists()) {
            BitmapUtils.deleteImageFile(this, mTempImagePath)
        }
        if (mBackupTempImagePath != null && File(mBackupTempImagePath!!).exists()) {
            BitmapUtils.deleteImageFile(this, mBackupTempImagePath)
        }
        if (mIsImageChanged && !TextUtils.isEmpty(mOldDescription)) {
            BitmapUtils.deleteImageFile(this, mOldDescription)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.add_note_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (mIsChanged) {
                    onBackPressed()
                } else {
                    NavUtils.navigateUpFromSameTask(this)
                }
                return true
            }
            R.id.save_action -> insertImageNote()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (mIsChanged) {
            ViewUtils.showUnsavedChangesDialog(this)
        } else {
            super.onBackPressed()
        }
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun insertImageNote() {
        val noteTitle = mImageBinding.noteTitleInput.text.toString().trim { it <= ' ' }
        val dateTimeString = mImageBinding.dateTimeTv.text.toString()
        if (!noteTitle.matches(Regex("[A-Za-z0-9 ]+")) || noteTitle.matches(Regex("[0-9 ]+"))) {
            Toast.makeText(this, getString(R.string.title_regex_error), Toast.LENGTH_LONG).show()
            return
        }
        if (TextUtils.isEmpty(noteTitle)) {
            Toast.makeText(this, getString(R.string.empty_title_error), Toast.LENGTH_LONG).show()
            return
        }
        if (mResultBitmap == null) {
            Toast.makeText(this, getString(R.string.empty_image_error), Toast.LENGTH_LONG).show()
            return
        }
        val note = if (mIsEditing) mNote else Note()
        note.noteTitle = noteTitle
        note.description = saveImageToStorage()
        val timeToRemind = getRelativeTimeFromNow(dateTimeString)
        if (timeToRemind < 0 && isNotificationEnabled) {
            Toast.makeText(this, getString(R.string.notification_time_error), Toast.LENGTH_LONG).show()
            return
        } else if (isNotificationEnabled && timeToRemind == 0) {
            Toast.makeText(this, getString(R.string.notification_error), Toast.LENGTH_LONG).show()
            return
        } else if (isNotificationEnabled) {
            note.remainingTimeToRemind = timeToRemind
            note.reminderDateTime = dateTimeString
        }
        if (!mIsEditing) {
            note.noteType = getString(R.string.image_note)
            note.dateCreated = System.currentTimeMillis()
            note.dateModified = 0
            AppExecutors.getInstance().diskIO().execute {
                note.noteId = NoteRepository.instance?.insertNote(note)?.toInt()!!
                runOnUiThread {
                    if (note.noteId > 0) {
                        if (timeToRemind > 0) {
                            ReminderUtils.scheduleNoteReminder(this@AddImageNote, note)
                        }
                        Toast.makeText(this@AddImageNote, getString(R.string.note_created_msg), Toast.LENGTH_LONG).show()
                        finish()
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    }
                }
            }
            return
        }
        note.dateModified = System.currentTimeMillis()
        mIsImageChanged = true
        AppExecutors.getInstance().diskIO().execute {
            val rowsUpdated = (NoteRepository.instance?.updateNote(note))!!
            runOnUiThread {
                if (rowsUpdated > 0) {
                    if (timeToRemind > 0) {
                        ReminderUtils.scheduleNoteReminder(this@AddImageNote, note)
                    }
                    Toast.makeText(this@AddImageNote, getString(R.string.note_updated_msg), Toast.LENGTH_LONG).show()
                    NoteService.startActionUpdateWidget(this@AddImageNote)
                    finish()
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                }
            }
        }
    }

    private fun saveImageToStorage(): String {
        // Save the image
        return BitmapUtils.saveImage(this, mResultBitmap)
    }

    override fun selectedDate(date: String) {
        dateTime = date
    }

    override fun selectedTime(time: String) {
        dateTime += " $time"
    }

    override fun dateTimeSelected(isSelected: Boolean) {
        if (isSelected) {
            mImageBinding.dateTimeTv.text = dateTime
        }
    }

    companion object {
        private const val RC_STORAGE_PERMISSION = 100
        private const val FILEPROVIDER_AUTHORITY = "com.intkhabahmed.fileprovider"
        private const val RC_CAPTURE_IMAGE = 101
    }
}