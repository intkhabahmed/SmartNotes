package com.intkhabahmed.smartnotes.database

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.intkhabahmed.smartnotes.models.Note
import com.intkhabahmed.smartnotes.utils.AppConstants
import com.intkhabahmed.smartnotes.utils.AppExecutors
import com.intkhabahmed.smartnotes.utils.Global

class NoteRepository {
    fun getNotesByTypeAndAvailability(type: String?, trashed: Int): DataSource.Factory<Int, Note> {
        if (TextUtils.isEmpty(type)) {
            return Global.getDbInstance().notesDao().getNotesByAvailability(trashed)
        }
        return if (Global.getSortCriteria().contains(AppConstants.ASC)) {
            Global.getDbInstance().notesDao().getNotesByTypeAndAvailabilityInAscendingOrder(type, trashed,
                    Global.getSortCriteria())
        } else Global.getDbInstance().notesDao().getNotesByTypeAndAvailabilityInDescendingOrder(type, trashed,
                Global.getSortCriteria())
    }

    fun getNotesByTitleAndAvailability(title: String?, trashed: Int): DataSource.Factory<Int, Note> {
        return Global.getDbInstance().notesDao().getNotesByTitleAndAvailability(title, trashed)
    }

    fun insertNote(note: Note?): Long? {
        return Global.getDbInstance().notesDao().insertNote(note)
    }

    fun updateNote(note: Note?): Int? {
        return Global.getDbInstance().notesDao().updateNote(note)
    }

    fun deleteNote(note: Note?) {
        Global.getDbInstance().notesDao().deleteNote(note)
    }

    fun moveNoteToTrash(note: Note) {
        note.trashed = 1
        AppExecutors.getInstance().diskIO().execute { Global.getDbInstance().notesDao().updateNote(note) }
    }

    fun recoverNoteFromTrash(note: Note) {
        note.trashed = 0
        AppExecutors.getInstance().diskIO().execute { Global.getDbInstance().notesDao().updateNote(note) }
    }

    fun getNoteById(noteId: Int): LiveData<Note> {
        return Global.getDbInstance().notesDao().getNoteById(noteId)
    }

    fun emptyTrash() {
        AppExecutors.getInstance().diskIO().execute { Global.getDbInstance().notesDao().deleteAll() }
    }

    companion object {
        private var sInstance: NoteRepository? = null
        private val LOCK = Any()

        @JvmStatic
        val instance: NoteRepository?
            get() {
                if (sInstance == null) {
                    synchronized(LOCK) { sInstance = NoteRepository() }
                }
                return sInstance
            }
    }
}