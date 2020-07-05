package com.intkhabahmed.smartnotes.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.intkhabahmed.smartnotes.R
import com.intkhabahmed.smartnotes.database.NoteRepository.Companion.instance
import com.intkhabahmed.smartnotes.databinding.NoteDetailLayoutBinding
import com.intkhabahmed.smartnotes.models.Note
import com.intkhabahmed.smartnotes.ui.AddSimpleNote
import com.intkhabahmed.smartnotes.utils.NoteUtils.getFormattedTime
import com.intkhabahmed.smartnotes.utils.NoteUtils.getRelativeTimeFromNow
import com.intkhabahmed.smartnotes.utils.ViewUtils
import com.intkhabahmed.smartnotes.viewmodels.NoteViewModel
import com.intkhabahmed.smartnotes.viewmodels.NoteViewModelFactory

class SimpleNotesDetailFragment : Fragment() {
    private var mNote: Note? = null
    private var mNoteId = 0
    private lateinit var mDetailBinding: NoteDetailLayoutBinding
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
            mNote = note
            if (mNote!!.trashed == 1) {
                setHasOptionsMenu(false)
            }
            setupUI()
        })
    }

    private fun setupUI() {
        mDetailBinding.run {
            tvNoteDescription.visibility = View.VISIBLE
            tvNoteTitle.text = mNote?.noteTitle
            tvNoteDescription.text = mNote!!.description
            tvDateCreated.text = getFormattedTime(mNote?.dateCreated ?: 0L)
            tvDateModified.text = if (mNote?.dateModified != 0L) getFormattedTime(mNote?.dateModified ?: 0L) else "-"
            tvNotification.text = if (mNote?.reminderDateTime != null) getFormattedTime(getRelativeTimeFromNow(mNote?.reminderDateTime) * 1000 + System.currentTimeMillis(),
                    System.currentTimeMillis()) else getString(R.string.notification_not_set)
            if (mNote!!.trashed == 1) {
                editNoteButton.hide()
            }
            editNoteButton.setOnClickListener {
                val intent = Intent(parentActivity, AddSimpleNote::class.java)
                intent.putExtra(Intent.EXTRA_TEXT, mNote)
                startActivity(intent)
                parentActivity!!.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            bannerAdView = AdView(parentActivity, getString(R.string.simple_detail_banner_placement_id), AdSize.BANNER_HEIGHT_50)

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
                instance!!.moveNoteToTrash(mNote!!)
                Toast.makeText(parentActivity, getString(R.string.moved_to_trash), Toast.LENGTH_LONG).show()
                parentActivity!!.supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
            ViewUtils.showDeleteConfirmationDialog(requireContext(), deleteListener, getString(R.string.delete_dialog_message))
        }
        return super.onOptionsItemSelected(item)
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