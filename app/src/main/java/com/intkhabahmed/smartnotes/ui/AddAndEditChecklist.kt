package com.intkhabahmed.smartnotes.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources.Theme
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intkhabahmed.smartnotes.R
import com.intkhabahmed.smartnotes.database.NoteRepository
import com.intkhabahmed.smartnotes.databinding.ActivityAddAndEditChecklistBinding
import com.intkhabahmed.smartnotes.models.ChecklistItem
import com.intkhabahmed.smartnotes.models.Note
import com.intkhabahmed.smartnotes.services.NoteService
import com.intkhabahmed.smartnotes.utils.AppExecutors
import com.intkhabahmed.smartnotes.utils.DateTimeListener
import com.intkhabahmed.smartnotes.utils.Global
import com.intkhabahmed.smartnotes.utils.NoteUtils.getRelativeTimeFromNow
import com.intkhabahmed.smartnotes.utils.ReminderUtils
import com.intkhabahmed.smartnotes.utils.ViewUtils
import kotlinx.android.synthetic.main.activity_add_and_edit_checklist.date_time_picker_btn
import java.util.*

class AddAndEditChecklist : AppCompatActivity(), DateTimeListener {
    private var mIsEditing = false
    private lateinit var mNote: Note
    private var mIsChanged = false
    private var mTrashed = 0
    private var menuItem: MenuItem? = null
    private var mItems: TreeMap<String, ChecklistItem>? = null
    private var dateTime: String? = null
    private var isNotificationEnabled = false
    private lateinit var mChecklistBinding: ActivityAddAndEditChecklistBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mChecklistBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_and_edit_checklist)
        val toolbar = mChecklistBinding.toolbar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.let {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_white_black_24dp)
            actionBar.setTitle(R.string.checklist)
        }
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (mTrashed == 0) {
                    mIsChanged = (!TextUtils.isEmpty(mChecklistBinding.checklistItem.text.toString().trim { it <= ' ' })
                            || !TextUtils.isEmpty(mChecklistBinding.checklistTitle.text.toString().trim { it <= ' ' }))
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        }
        mChecklistBinding.checklistTitle.addTextChangedListener(textWatcher)
        date_time_picker_btn.setOnClickListener { ViewUtils.showDatePicker(this@AddAndEditChecklist, this@AddAndEditChecklist) }
        mItems = TreeMap()
        mChecklistBinding.addChecklistButton.setOnClickListener {
            val checklistItem = mChecklistBinding.checklistItem.text.toString().trim { it <= ' ' }.toLowerCase(Locale.getDefault())
            if (TextUtils.isEmpty(checklistItem)) {
                Toast.makeText(this@AddAndEditChecklist, getString(R.string.empty_item_error),
                        Toast.LENGTH_LONG).show()
            } else if (!mItems!!.containsKey(checklistItem)) {
                mChecklistBinding.checklistItem.setText("")
                mIsChanged = true
                if (mItems!!.size == 0) {
                    mChecklistBinding.itemsLabelTv.visibility = View.VISIBLE
                    mChecklistBinding.checklistContainer.visibility = View.VISIBLE
                }
                addChecklist(checklistItem, false)
            } else {
                Toast.makeText(this@AddAndEditChecklist, getString(R.string.duplicate_entry_error),
                        Toast.LENGTH_LONG).show()
            }
        }
        val colorStateList = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)), intArrayOf(
                ViewUtils.getColorFromAttribute(this, R.attr.primaryTextColor),
                ViewUtils.getColorFromAttribute(this, R.attr.colorAccent)))
        CompoundButtonCompat.setButtonTintList(mChecklistBinding.enableNotificationCb, colorStateList)
        mChecklistBinding.enableNotificationCb.setOnCheckedChangeListener { _, isChecked ->
            isNotificationEnabled = isChecked
            if (isChecked) {
                mChecklistBinding.notificationGroup.visibility = View.VISIBLE
            } else {
                mChecklistBinding.notificationGroup.visibility = View.GONE
            }
        }
        val intent = intent
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            mNote = intent.getParcelableExtra(Intent.EXTRA_TEXT) as Note
            mIsEditing = true
            Handler().postDelayed({ populateChecklist() }, 50)
        } else {
            mIsEditing = false
        }
    }

    private fun addChecklist(checklistItem: String, checked: Boolean) {
        if (TextUtils.isEmpty(checklistItem)) {
            Toast.makeText(this, getString(R.string.empty_task_error), Toast.LENGTH_LONG).show()
            return
        }
        val checkBoxContainer = LinearLayout(this)
        checkBoxContainer.orientation = LinearLayout.HORIZONTAL
        checkBoxContainer.contentDescription = checklistItem
        val removeButton = ImageButton(this)
        removeButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_clear_24dp))
        removeButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
        removeButton.setPadding(0, 10, 0, 5)
        val checkBox = CheckBox(this)
        checkBox.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        checkBox.text = checklistItem
        checkBox.isChecked = checked
        if (checked) {
            checkBox.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }
        checkBoxContainer.addView(removeButton, 0)
        checkBoxContainer.addView(checkBox, 1)
        if (Build.VERSION.SDK_INT >= 23) {
            checkBox.setTextAppearance(R.style.TextAppearance_AppCompat_Large)
        }
        val colorStateList = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)), intArrayOf(
                ViewUtils.getColorFromAttribute(this, R.attr.primaryTextColor),
                ViewUtils.getColorFromAttribute(this, R.attr.colorAccent)))
        CompoundButtonCompat.setButtonTintList(checkBox, colorStateList)
        checkBox.setTextColor(ViewUtils.getColorFromAttribute(this, R.attr.primaryTextColor))
        removeButton.setColorFilter(ViewUtils.getColorFromAttribute(this, R.attr.iconPlaceHolder))
        mChecklistBinding.checklistContainer.addView(checkBoxContainer)
        mItems!![checklistItem] = ChecklistItem(checklistItem, checkBox.isChecked)
        if (mTrashed == 0) {
            checkBox.setOnCheckedChangeListener { _, b ->
                if (b) {
                    checkBox.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    checkBox.paintFlags = 0
                }
                mItems!![checklistItem]?.isChecked = b
            }
        } else {
            checkBox.isEnabled = false
            removeButton.visibility = View.GONE
        }
        removeButton.setOnClickListener {
            if (mTrashed == 0) {
                mItems!!.remove(checklistItem)
                mIsChanged = true
                mChecklistBinding.checklistContainer.removeView(checkBoxContainer)
                if (mItems!!.size == 0) {
                    if (!mIsEditing) {
                        mIsChanged = false
                    }
                    mChecklistBinding.itemsLabelTv.visibility = View.GONE
                    mChecklistBinding.checklistContainer.visibility = View.GONE
                }
            }
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.add_note_menu, menu)
        menuItem = menu.findItem(R.id.save_action)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> {
                if (mIsChanged) {
                    onBackPressed()
                } else {
                    NavUtils.navigateUpFromSameTask(this)
                }
                return true
            }
            R.id.save_action -> insertChecklist()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun insertChecklist() {
        if (mItems?.size!! < 1) {
            Toast.makeText(this, getString(R.string.empty_task_list_error), Toast.LENGTH_LONG).show()
            return
        }
        val checklistData = Gson().toJson(mItems!!.values)
        val checklistTitle = mChecklistBinding.checklistTitle.text.toString().trim { it <= ' ' }
        val dateTimeString = mChecklistBinding.dateTimeTv.text.toString()
        if (TextUtils.isEmpty(checklistTitle)) {
            Toast.makeText(this, getString(R.string.list_title_error), Toast.LENGTH_LONG).show()
            return
        }
        val note = if (mIsEditing) mNote else Note()
        note.noteTitle = checklistTitle
        note.description = checklistData
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
            note.noteType = getString(R.string.checklist)
            note.dateCreated = System.currentTimeMillis()
            note.dateModified = 0
            AppExecutors.getInstance().diskIO().execute {
                note.noteId = NoteRepository.instance?.insertNote(note)?.toInt()!!
                runOnUiThread {
                    if (note.noteId > 0) {
                        if (timeToRemind > 0) {
                            ReminderUtils.scheduleNoteReminder(this@AddAndEditChecklist, note)
                        }
                        Toast.makeText(this@AddAndEditChecklist, getString(R.string.note_created_msg), Toast.LENGTH_LONG).show()
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
                        ReminderUtils.scheduleNoteReminder(this@AddAndEditChecklist, note)
                    }
                    Toast.makeText(this@AddAndEditChecklist, getString(R.string.note_updated_msg), Toast.LENGTH_LONG).show()
                    NoteService.startActionUpdateWidget(this@AddAndEditChecklist)
                    finish()
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                }
            }
        }
    }

    private fun populateChecklist() {
        mTrashed = mNote.trashed
        if (mTrashed == 1) {
            mChecklistBinding.checklistTitle.isEnabled = false
            mChecklistBinding.checklistItem.visibility = View.GONE
            mChecklistBinding.addChecklistButton.hide()
            menuItem!!.isVisible = false
        }
        mChecklistBinding.checklistTitle.setText(mNote.noteTitle)
        if (!TextUtils.isEmpty(mNote.reminderDateTime) && getRelativeTimeFromNow(mNote.reminderDateTime) > 0) {
            mChecklistBinding.dateTimeTv.text = mNote.reminderDateTime
            mChecklistBinding.enableNotificationCb.isChecked = true
        } else {
            mChecklistBinding.dateTimeTv.text = getString(R.string.notification_desc)
            mChecklistBinding.enableNotificationCb.isChecked = false
        }
        val checklistItems = Gson().fromJson<List<ChecklistItem>>(mNote.description, object : TypeToken<List<ChecklistItem?>?>() {}.type)
        if (checklistItems.isNotEmpty()) {
            mChecklistBinding.itemsLabelTv.visibility = View.VISIBLE
            mChecklistBinding.checklistContainer.visibility = View.VISIBLE
        }
        for (item: ChecklistItem in checklistItems) {
            addChecklist(item.title, item.isChecked)
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
            mChecklistBinding.dateTimeTv.text = dateTime
        }
    }
}