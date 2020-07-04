package com.intkhabahmed.smartnotes.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.intkhabahmed.smartnotes.R
import com.intkhabahmed.smartnotes.adapters.NotesAdapter
import com.intkhabahmed.smartnotes.database.NoteRepository
import com.intkhabahmed.smartnotes.databinding.NotesRecyclerViewBinding
import com.intkhabahmed.smartnotes.models.Note
import com.intkhabahmed.smartnotes.ui.AddSimpleNote
import com.intkhabahmed.smartnotes.utils.NoteUtils.shareNote
import com.intkhabahmed.smartnotes.utils.ViewUtils
import com.intkhabahmed.smartnotes.viewmodels.NotesViewModel
import com.intkhabahmed.smartnotes.viewmodels.NotesViewModelFactory

/**
 * Created by INTKHAB on 01-10-2018.
 */
class SimpleNotesFragment : Fragment(), NotesAdapter.OnItemClickListener {
    private lateinit var mNotesAdapter: NotesAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mNotesBinding: NotesRecyclerViewBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mNotesBinding = DataBindingUtil.inflate(inflater, R.layout.notes_recycler_view, container, false)
        return mNotesBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mNotesAdapter = NotesAdapter(this)
        mNotesBinding.run {
            mRecyclerView = mNotesBinding.recyclerView
            mRecyclerView.apply {
                val linearLayoutManager = LinearLayoutManager(parentActivity, LinearLayoutManager.VERTICAL, false)
                layoutManager = linearLayoutManager
                adapter = mNotesAdapter
                setHasFixedSize(true)
            }
            addButton.show()
            addButton.setOnClickListener {
                val intent = Intent(parentActivity, AddSimpleNote::class.java)
                startActivity(intent)
                parentActivity!!.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
        setupViewModel(false)
    }

    private fun setupViewModel(isSortCriteriaChanged: Boolean) {
        mNotesBinding.progressBar.visibility = View.VISIBLE
        val factory = NotesViewModelFactory(getString(R.string.simple_note), 0)
        val notesViewModel = ViewModelProvider(this, factory).get(NotesViewModel::class.java)
        if (isSortCriteriaChanged) {
            notesViewModel.setNotes(getString(R.string.simple_note), 0)
        }
        notesViewModel.notes.observe(viewLifecycleOwner, Observer { notes ->
            mNotesBinding.progressBar.visibility = View.GONE
            mNotesAdapter.submitList(notes)
            if (notes != null && notes.size > 0) {
                ViewUtils.hideEmptyView(mRecyclerView, mNotesBinding.emptyView)
            } else {
                ViewUtils.showEmptyView(mRecyclerView, mNotesBinding.emptyView)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        mNotesBinding.addButton.show()
    }

    fun updateSimpleNotesFragment() {
        setupViewModel(true)
    }

    override fun onItemClick(noteId: Int, noteType: String) {
        val simpleNotesDetailFragment = SimpleNotesDetailFragment()
        simpleNotesDetailFragment.setNoteId(noteId)
        parentActivity!!.supportFragmentManager.beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragment_layout, simpleNotesDetailFragment)
                .commit()
    }

    override fun onMenuItemClick(view: View, note: Note) {
        val popupMenu = PopupMenu(parentActivity, view)
        popupMenu.apply {
            inflate(R.menu.item_menu)
            setOnMenuItemClickListener { menuItem ->
                val id = menuItem.itemId
                when (id) {
                    R.id.delete_note -> {
                        val deleteListener = DialogInterface.OnClickListener { dialogInterface, i ->
                            NoteRepository.instance?.moveNoteToTrash(note)
                            showSnackBar(note)
                        }
                        ViewUtils.showDeleteConfirmationDialog(requireContext(), deleteListener, getString(R.string.delete_dialog_message))
                    }
                    R.id.share_note -> shareNote(parentActivity!!, note.description)
                }
                false
            }
            show()
        }
    }

    private fun showSnackBar(note: Note) {
        val snackbar = Snackbar.make(mNotesBinding.addButton, getString(R.string.moved_to_trash), Snackbar.LENGTH_LONG)
        snackbar.apply {
            setAction(getString(R.string.undo)) {
                NoteRepository.instance?.recoverNoteFromTrash(note)
                Snackbar.make(mNotesBinding.addButton, getString(R.string.restored), Snackbar.LENGTH_LONG).show()
            }
            setActionTextColor(ViewUtils.getColorFromAttribute(requireContext(), R.attr.colorAccent))
            show()
        }
    }

    private val parentActivity: FragmentActivity?
        get() = activity
}