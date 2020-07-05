package com.intkhabahmed.smartnotes.fragments

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intkhabahmed.smartnotes.R
import com.intkhabahmed.smartnotes.adapters.NotesAdapter
import com.intkhabahmed.smartnotes.database.NoteRepository.Companion.instance
import com.intkhabahmed.smartnotes.databinding.NotesRecyclerViewBinding
import com.intkhabahmed.smartnotes.models.ChecklistItem
import com.intkhabahmed.smartnotes.models.Note
import com.intkhabahmed.smartnotes.utils.BitmapUtils
import com.intkhabahmed.smartnotes.utils.NoteUtils.shareNote
import com.intkhabahmed.smartnotes.utils.ViewUtils
import com.intkhabahmed.smartnotes.viewmodels.SearchNotesViewModel
import com.intkhabahmed.smartnotes.viewmodels.SearchNotesViewModelFactory

class SearchFragment : Fragment(), NotesAdapter.OnItemClickListener, SearchView.OnQueryTextListener {
    private lateinit var mNotesAdapter: NotesAdapter
    private lateinit var mRecyclerView: RecyclerView
    private var mSearchView: SearchView? = null
    private var mFilterText: String? = null
    private lateinit var mNotesBinding: NotesRecyclerViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mFilterText = savedInstanceState.getString(BUNDLE_EXTRA)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mNotesBinding = DataBindingUtil.inflate(inflater, R.layout.notes_recycler_view, container, false)
        return mNotesBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        mNotesAdapter = NotesAdapter(this)
        mRecyclerView = mNotesBinding.recyclerView
        mNotesBinding.recyclerView.apply {
            val linearLayoutManager = LinearLayoutManager(parentActivity, LinearLayoutManager.VERTICAL, false)
            layoutManager = linearLayoutManager
            adapter = mNotesAdapter
            setHasFixedSize(true)
        }
        setupViewModel(false)
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupViewModel(isQueryChanged: Boolean) {
        val factory = SearchNotesViewModelFactory(mFilterText, 0)
        val notesViewModel = ViewModelProvider(this, factory).get(SearchNotesViewModel::class.java)
        if (isQueryChanged) {
            notesViewModel.setNotes(mFilterText, 0)
        }
        notesViewModel.notes.observe(viewLifecycleOwner, Observer { notes ->
            mNotesAdapter.submitList(notes)
            if (notes != null && notes.size > 0) {
                hideEmptyView()
            } else {
                showEmptyView()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        parentActivity!!.menuInflater.inflate(R.menu.search_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
        val searchViewItem = menu.findItem(R.id.search_menu)
        searchViewItem.expandActionView()
        mSearchView = menu.findItem(R.id.search_menu).actionView as SearchView
        mSearchView!!.queryHint = getString(R.string.search_hint)
        val searchEditText = mSearchView!!.findViewById<EditText>(R.id.search_src_text)
        searchEditText.setHintTextColor(Color.WHITE)
        searchEditText.setTextColor(Color.WHITE)
        val closedBtn = mSearchView!!.findViewById<ImageView>(R.id.search_close_btn)
        closedBtn.setColorFilter(Color.WHITE)
        mSearchView!!.maxWidth = 4000
        mSearchView!!.setOnQueryTextListener(this)
        if (!TextUtils.isEmpty(mFilterText)) {
            mSearchView!!.setQuery(mFilterText, false)
        }
        searchViewItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                return false
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                mSearchView!!.setQuery("", false)
                parentActivity!!.supportFragmentManager.popBackStack(HomePageFragment::class.java.name, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                return true
            }
        })
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return true
    }

    override fun onQueryTextChange(query: String): Boolean {
        mFilterText = if (TextUtils.isEmpty(query)) null else query
        setupViewModel(true)
        return true
    }

    private fun showEmptyView() {
        mRecyclerView.visibility = View.INVISIBLE
        mNotesBinding.searchErrorView.visibility = View.INVISIBLE
        if (!TextUtils.isEmpty(mFilterText)) {
            mNotesBinding.searchErrorView.visibility = View.VISIBLE
        }
    }

    private fun hideEmptyView() {
        mRecyclerView.visibility = View.VISIBLE
        mNotesBinding.searchErrorView.visibility = View.INVISIBLE
    }

    override fun onMenuItemClick(view: View, note: Note) {
        val popupMenu = PopupMenu(parentActivity, view)
        popupMenu.apply {
            inflate(R.menu.item_menu)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.delete_note -> {
                        val deleteListener = DialogInterface.OnClickListener { dialogInterface, i ->
                            instance!!.moveNoteToTrash(note)
                            showSnackBar(note)
                        }
                        ViewUtils.showDeleteConfirmationDialog(requireContext(), deleteListener, getString(R.string.delete_dialog_message))
                    }
                    R.id.share_note -> if (note.noteType != null && note.noteType == getString(R.string.image_note)) {
                        BitmapUtils.shareImage(parentActivity, note.description)
                    } else if (note.noteType != null && note.noteType == getString(R.string.checklist)) {
                        val tasks = StringBuilder()
                        tasks.append(note.noteTitle)
                        tasks.append("\n_____________________")
                        val checklistItems = Gson().fromJson<List<ChecklistItem>>(note.description, object : TypeToken<List<ChecklistItem?>?>() {}.type)
                        if (checklistItems != null) {
                            for (item in checklistItems) {
                                tasks.append("\n")
                                tasks.append(item.title)
                            }
                        }
                        shareNote(requireContext(), tasks.toString())
                    } else {
                        shareNote(requireContext(), note.description)
                    }
                }
                false
            }
            show()
        }
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

    private fun showSnackBar(note: Note) {
        val snackbar = Snackbar.make(mNotesBinding.rootFrameLayout, getString(R.string.moved_to_trash), Snackbar.LENGTH_LONG)
        snackbar.apply {
            setAction(getString(R.string.undo)) {
                instance!!.recoverNoteFromTrash(note)
                Snackbar.make(mNotesBinding.addButton, getString(R.string.restored), Snackbar.LENGTH_LONG).show()
            }
            setActionTextColor(ViewUtils.getColorFromAttribute(requireContext(), R.attr.colorAccent))
            show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(BUNDLE_EXTRA, mFilterText)
        super.onSaveInstanceState(outState)
    }

    private val parentActivity: FragmentActivity?
        get() = activity

    companion object {
        private const val BUNDLE_EXTRA = "search-query"
    }
}