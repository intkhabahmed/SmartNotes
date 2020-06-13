package com.intkhabahmed.smartnotes.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources.Theme
import android.os.Bundle
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
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import androidx.core.widget.CompoundButtonCompat
import androidx.databinding.DataBindingUtil
import com.intkhabahmed.smartnotes.R
import com.intkhabahmed.smartnotes.database.NoteRepository
import com.intkhabahmed.smartnotes.databinding.ActivityAddSimpleNoteBinding
import com.intkhabahmed.smartnotes.models.Note
import com.intkhabahmed.smartnotes.services.NoteService
import com.intkhabahmed.smartnotes.utils.AppExecutors
import com.intkhabahmed.smartnotes.utils.DateTimeListener
import com.intkhabahmed.smartnotes.utils.Global
import com.intkhabahmed.smartnotes.utils.NoteUtils.getRelativeTimeFromNow
import com.intkhabahmed.smartnotes.utils.ReminderUtils
import com.intkhabahmed.smartnotes.utils.ViewUtils

class AddSimpleNote() : AppCompatActivity(), DateTimeListener {
    private var mIsEditing = false
    private lateinit var mNote: Note
    private var mIsChanged = false
    private var dateTime: String? = null
    private var isNotificationEnabled = false
    private lateinit var mSimpleBinding: ActivityAddSimpleNoteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSimpleBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_simple_note)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_arrow_white_black_24dp)
            it.setTitle(R.string.simple_note)
        }
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                mIsChanged = (!TextUtils.isEmpty(mSimpleBinding.noteTitleInput.text.toString().trim { it <= ' ' })
                        || !TextUtils.isEmpty(mSimpleBinding.noteDescriptionInput.text.toString().trim { it <= ' ' }))
            }

            override fun afterTextChanged(editable: Editable) {}
        }
        mSimpleBinding.noteTitleInput.addTextChangedListener(textWatcher)
        mSimpleBinding.noteDescriptionInput.addTextChangedListener(textWatcher)
        val notificationCb = findViewById<CheckBox>(R.id.enable_notification_cb)
        val colorStateList = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)), intArrayOf(
                ViewUtils.getColorFromAttribute(this, R.attr.primaryTextColor),
                ViewUtils.getColorFromAttribute(this, R.attr.colorAccent)))
        CompoundButtonCompat.setButtonTintList(notificationCb, colorStateList)
        notificationCb.setOnCheckedChangeListener { _, isChecked ->
            isNotificationEnabled = isChecked
            if (isChecked) {
                mSimpleBinding.notificationGroup.visibility = View.VISIBLE
            } else {
                mSimpleBinding.notificationGroup.visibility = View.GONE
            }
        }
        val intent = intent
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            mIsEditing = true
            mNote = intent.getParcelableExtra(Intent.EXTRA_TEXT) as Note
            mSimpleBinding.noteTitleInput.setText(mNote.noteTitle)
            mSimpleBinding.noteDescriptionInput.setText(mNote.description)
            if (!TextUtils.isEmpty(mNote.reminderDateTime) && getRelativeTimeFromNow(mNote.reminderDateTime) > 0) {
                mSimpleBinding.dateTimeTv.text = mNote.reminderDateTime
                notificationCb.isChecked = true
            } else {
                mSimpleBinding.dateTimeTv.text = getString(R.string.notification_desc)
                notificationCb.isChecked = false
            }
        } else {
            mIsEditing = false
        }
        val dateTimePickerBtn = findViewById<ImageButton>(R.id.date_time_picker_btn)
        dateTimePickerBtn.setOnClickListener { ViewUtils.showDatePicker(this, this) }
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
            R.id.save_action -> insertSimpleNote()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun insertSimpleNote() {
        val noteTitle = mSimpleBinding.noteTitleInput.text.toString().trim { it <= ' ' }
        val noteDescription = mSimpleBinding.noteDescriptionInput.text.toString().trim { it <= ' ' }
        val dateTimeString = mSimpleBinding.dateTimeTv.text.toString()
        if (TextUtils.isEmpty(noteTitle) || TextUtils.isEmpty(noteDescription)) {
            Toast.makeText(this, getString(R.string.mandatory_fields_error), Toast.LENGTH_LONG).show()
            return
        }
        val note = if (mIsEditing) mNote else Note()
        note.noteTitle = noteTitle
        note.description = noteDescription
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
            note.noteType = getString(R.string.simple_note)
            note.dateCreated = System.currentTimeMillis()
            note.dateModified = 0
            AppExecutors.getInstance().diskIO().execute {
                note.noteId = NoteRepository.instance?.insertNote(note)?.toInt()!!
                runOnUiThread {
                    if (note.noteId > 0) {
                        if (timeToRemind > 0) {
                            ReminderUtils.scheduleNoteReminder(this@AddSimpleNote, note)
                        }
                        Toast.makeText(this@AddSimpleNote, getString(R.string.note_created_msg), Toast.LENGTH_LONG).show()
                        finish()
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    }
                }
            }
            return
        }
        note.dateModified = System.currentTimeMillis()
        AppExecutors.getInstance().diskIO().execute {
            val rowsUpdated = (NoteRepository.instance?.updateNote(note))!!
            runOnUiThread {
                if (rowsUpdated > 0) {
                    if (timeToRemind > 0) {
                        ReminderUtils.scheduleNoteReminder(this@AddSimpleNote, note)
                    }
                    Toast.makeText(this@AddSimpleNote, getString(R.string.note_updated_msg), Toast.LENGTH_LONG).show()
                    NoteService.startActionUpdateWidget(this@AddSimpleNote)
                    finish()
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (mIsChanged) {
            ViewUtils.showUnsavedChangesDialog(this)
        } else {
            super.onBackPressed()
        }
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun selectedDate(date: String) {
        dateTime = date
    }

    override fun selectedTime(time: String) {
        dateTime += " $time"
    }

    override fun dateTimeSelected(isSelected: Boolean) {
        if (isSelected) {
            mSimpleBinding.dateTimeTv.text = dateTime
        }
    }
}