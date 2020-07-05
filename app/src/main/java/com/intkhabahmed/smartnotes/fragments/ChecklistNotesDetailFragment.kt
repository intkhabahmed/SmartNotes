package com.intkhabahmed.smartnotes.fragments

import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.widget.CompoundButtonCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intkhabahmed.smartnotes.R
import com.intkhabahmed.smartnotes.database.NoteRepository
import com.intkhabahmed.smartnotes.databinding.NoteDetailLayoutBinding
import com.intkhabahmed.smartnotes.models.ChecklistItem
import com.intkhabahmed.smartnotes.models.Note
import com.intkhabahmed.smartnotes.services.NoteService
import com.intkhabahmed.smartnotes.ui.AddAndEditChecklist
import com.intkhabahmed.smartnotes.utils.AppExecutors
import com.intkhabahmed.smartnotes.utils.NoteUtils
import com.intkhabahmed.smartnotes.utils.ViewUtils
import com.intkhabahmed.smartnotes.viewmodels.NoteViewModel
import com.intkhabahmed.smartnotes.viewmodels.NoteViewModelFactory
import java.util.*

class ChecklistNotesDetailFragment : Fragment() {
    private var mNote: Note? = null
    private var mNoteId = 0
    private lateinit var mDetailBinding: NoteDetailLayoutBinding
    private val mItems: TreeMap<String, ChecklistItem> by lazy {
        TreeMap<String, ChecklistItem>()
    }
    private var isChecklistPressed = false
    private lateinit var bannerAdView: AdView
    fun setNoteId(noteId: Int) {
        mNoteId = noteId
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mNoteId = savedInstanceState.getInt(BUNDLE_DATA)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mDetailBinding = DataBindingUtil.inflate(inflater, R.layout.note_detail_layout, container, false)
        return mDetailBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupNoteViewModel()
    }

    private fun setupNoteViewModel() {
        val factory = NoteViewModelFactory(mNoteId)
        val noteViewModel = ViewModelProvider(this, factory).get(NoteViewModel::class.java)
        noteViewModel.note?.observe(viewLifecycleOwner, Observer { note ->
            if (note != null) {
                mNote = note
                if (mNote!!.trashed == 1) {
                    setHasOptionsMenu(false)
                }
                if (!isChecklistPressed) {
                    mItems.clear()
                    mDetailBinding.checklistContainer.removeAllViews()
                    setupUI()
                }
            }
        })
    }

    private fun setupUI() {
        mDetailBinding.run {
            checklistContainer.visibility = View.VISIBLE
            tvNoteTitle.text = mNote?.noteTitle
            Handler().postDelayed({ populateChecklistData() }, 200)
            tvDateCreated.text = NoteUtils.getFormattedTime(mNote?.dateCreated ?: 0L)
            tvDateModified.text = if (mNote?.dateModified != 0L) {
                NoteUtils.getFormattedTime(mNote?.dateModified
                        ?: 0L)
            } else {
                "-"
            }
            tvNotification.text = if (mNote?.reminderDateTime != null) {
                NoteUtils.getFormattedTime(NoteUtils.getRelativeTimeFromNow(mNote?.reminderDateTime) * 1000 + System.currentTimeMillis(),
                        System.currentTimeMillis())
            } else {
                getString(R.string.notification_not_set)
            }
            if (mNote?.trashed == 1) {
                editNoteButton.hide()
            }
            editNoteButton.setOnClickListener {
                val intent = Intent(parentActivity, AddAndEditChecklist::class.java)
                intent.putExtra(Intent.EXTRA_TEXT, mNote)
                startActivity(intent)
                parentActivity!!.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            bannerAdView = AdView(parentActivity, getString(R.string.checklist_detail_banner_placement_id), AdSize.BANNER_HEIGHT_50)

            bannerAdView.run {
                // Add the ad view to your activity layout
                adView2.addView(this)

                // Request an ad
                loadAd()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(BUNDLE_DATA, mNoteId)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.detail_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.delete_menu) {
            val deleteListener = DialogInterface.OnClickListener { dialogInterface, i ->
                NoteRepository.instance?.moveNoteToTrash(mNote!!)
                Toast.makeText(parentActivity, getString(R.string.moved_to_trash), Toast.LENGTH_LONG).show()
                parentActivity!!.supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
            ViewUtils.showDeleteConfirmationDialog(requireContext(), deleteListener, getString(R.string.delete_dialog_message))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun populateChecklistData() {
        val checklistItems = Gson().fromJson<List<ChecklistItem>>(mNote?.description, object : TypeToken<List<ChecklistItem?>?>() {}.type)
        for (item in checklistItems) {
            val checkBox = CheckBox(parentActivity)
            checkBox.apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                text = item.title
                isChecked = item.isChecked
                if (item.isChecked) {
                    paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                }
                mDetailBinding.checklistContainer.addView(this)
                if (Build.VERSION.SDK_INT >= 23) {
                    setTextAppearance(R.style.TextAppearance_AppCompat_Large)
                }
                val colorStateList = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)), intArrayOf(
                        ViewUtils.getColorFromAttribute(requireContext(), R.attr.primaryTextColor),
                        ViewUtils.getColorFromAttribute(requireContext(), R.attr.colorAccent)))
                CompoundButtonCompat.setButtonTintList(checkBox, colorStateList)
                setTextColor(ViewUtils.getColorFromAttribute(requireContext(), R.attr.primaryTextColor))
                mItems[item.title] = ChecklistItem(item.title, checkBox.isChecked)
                if (mNote?.trashed == 0) {
                    checkBox.setOnCheckedChangeListener { _, checked ->
                        paintFlags = if (checked) {
                            Paint.STRIKE_THRU_TEXT_FLAG
                        } else {
                            0
                        }
                        isChecklistPressed = true
                        mItems[item.title]?.isChecked = checked
                        mNote?.description = Gson().toJson(mItems.values)
                        AppExecutors.getInstance().diskIO().execute {
                            if (NoteRepository.instance?.updateNote(mNote)!! > 0) {
                                NoteService.startActionUpdateWidget(parentActivity)
                            }
                        }
                    }
                } else {
                    isEnabled = false
                }
            }
        }

    }

    override fun onDestroy() {
        if (::bannerAdView.isInitialized) {
            bannerAdView.destroy()
            mDetailBinding.adView2.removeView(bannerAdView)
        }
        super.onDestroy()
    }

    private val parentActivity: FragmentActivity?
        get() = activity

    companion object {
        private const val BUNDLE_DATA = "bundle-data"
    }
}