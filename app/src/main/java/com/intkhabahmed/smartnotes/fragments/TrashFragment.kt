package com.intkhabahmed.smartnotes.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intkhabahmed.smartnotes.R
import com.intkhabahmed.smartnotes.adapters.NotesAdapter
import com.intkhabahmed.smartnotes.database.NoteRepository.Companion.instance
import com.intkhabahmed.smartnotes.databinding.NotesRecyclerViewBinding
import com.intkhabahmed.smartnotes.models.Note
import com.intkhabahmed.smartnotes.ui.MainActivity
import com.intkhabahmed.smartnotes.utils.AppExecutors
import com.intkhabahmed.smartnotes.utils.BitmapUtils
import com.intkhabahmed.smartnotes.utils.ViewUtils
import com.intkhabahmed.smartnotes.viewmodels.NotesViewModel
import com.intkhabahmed.smartnotes.viewmodels.NotesViewModelFactory

class TrashFragment : Fragment(), NotesAdapter.OnItemClickListener {
    private lateinit var mNotesAdapter: NotesAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mNotesBinding: NotesRecyclerViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mNotesBinding = DataBindingUtil.inflate(inflater, R.layout.notes_recycler_view, container, false)
        return mNotesBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mNotesAdapter = NotesAdapter(parentActivity!!, this)
        mNotesBinding.run {
            mRecyclerView = mNotesBinding.recyclerView
            mRecyclerView.apply {
                val linearLayoutManager = LinearLayoutManager(parentActivity, LinearLayoutManager.VERTICAL, false)
                layoutManager = linearLayoutManager
                adapter = mNotesAdapter
                setHasFixedSize(true)
            }
        }
        parentActivity!!.setTitle(R.string.trash)
        setupViewModel()
    }

    private fun setupViewModel() {
        mNotesBinding.progressBar.visibility = View.VISIBLE
        val factory = NotesViewModelFactory("", 1)
        val notesViewModel = ViewModelProvider(this, factory).get(NotesViewModel::class.java)
        notesViewModel.notes.observe(viewLifecycleOwner, Observer { notes ->
            mNotesBinding.progressBar.visibility = View.GONE
            mNotesAdapter.submitList(notes)
            if (notes != null && notes.size > 0) {
                ViewUtils.hideEmptyView(mRecyclerView, mNotesBinding.trashEmptyView)
            } else {
                setHasOptionsMenu(false)
                ViewUtils.showEmptyView(mRecyclerView, mNotesBinding.trashEmptyView)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val listener = (parentActivity as MainActivity?)!!.currentFragmentListener
        listener.setCurrentFragment(TrashFragment::class.java.simpleName)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.trash_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.empty_trash) {
            val deleteListener = DialogInterface.OnClickListener { dialogInterface, i ->
                instance!!.emptyTrash()
                Toast.makeText(parentActivity, getString(R.string.all_items_deleted), Toast.LENGTH_LONG).show()
            }
            ViewUtils.showDeleteConfirmationDialog(requireContext(), deleteListener, getString(R.string.empty_trash_dialog_message))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(noteId: Int, noteType: String) {
        val fragment: Fragment
        fragment = when (noteType) {
            getString(R.string.checklist) -> {
                val checklistNotesDetailFragment = ChecklistNotesDetailFragment()
                checklistNotesDetailFragment.setNoteId(noteId)
                checklistNotesDetailFragment
            }
            getString(R.string.image_note) -> {
                val imageNotesDetailFragment = ImageNotesDetailFragment()
                imageNotesDetailFragment.setNoteId(noteId)
                imageNotesDetailFragment
            }
            else -> {
                val simpleNotesDetailFragment = SimpleNotesDetailFragment()
                simpleNotesDetailFragment.setNoteId(noteId)
                simpleNotesDetailFragment
            }
        }
        parentActivity!!.supportFragmentManager.beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragment_layout, fragment)
                .commit()
    }

    override fun onMenuItemClick(view: View, note: Note) {
        val popupMenu = PopupMenu(parentActivity, view)
        popupMenu.apply {
            inflate(R.menu.item_menu)
            menu.getItem(0).title = getString(R.string.delete_forever)
            menu.getItem(1).title = getString(R.string.restore)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.delete_note -> {
                        val deleteListener = DialogInterface.OnClickListener { dialogInterface, i ->
                            if (note.noteType != null && note.noteType == getString(R.string.image_note)) {
                                val imagePath = note.description
                                BitmapUtils.deleteImageFile(parentActivity, imagePath)
                            }
                            AppExecutors.getInstance().diskIO().execute { instance!!.deleteNote(note) }
                            Toast.makeText(parentActivity, getString(R.string.deleted_permanently), Toast.LENGTH_LONG).show()
                        }
                        ViewUtils.showDeleteConfirmationDialog(requireContext(), deleteListener, getString(R.string.permanently_delete_dialog_message))
                    }
                    R.id.share_note -> {
                        instance!!.recoverNoteFromTrash(note)
                        Toast.makeText(parentActivity, getString(R.string.restored), Toast.LENGTH_LONG).show()
                    }
                }
                false
            }
            show()
        }
    }

    private val parentActivity: FragmentActivity?
        get() = activity
}