package com.intkhabahmed.smartnotes.database

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.intkhabahmed.smartnotes.models.Note

@Dao
interface NotesDao {
    @Query("SELECT * FROM notes WHERE noteType = :type AND trash = :trashed ORDER BY " +
            "CASE :sortOrder " +
            "WHEN 'dateCreated ASC' THEN dateCreated " +
            "WHEN 'title ASC' THEN title " +
            "END ASC")
    fun getNotesByTypeAndAvailabilityInAscendingOrder(type: String?, trashed: Int, sortOrder: String?): DataSource.Factory<Int, Note>

    @Query("SELECT * FROM notes WHERE noteType = :type AND trash = :trashed ORDER BY " +
            "CASE :sortOrder " +
            "WHEN 'dateCreated DESC' THEN dateCreated " +
            "WHEN 'title DESC' THEN title " +
            "END DESC")
    fun getNotesByTypeAndAvailabilityInDescendingOrder(type: String?, trashed: Int, sortOrder: String?): DataSource.Factory<Int, Note>

    @Query("SELECT * FROM notes WHERE trash = :trashed")
    fun getNotesByAvailability(trashed: Int): DataSource.Factory<Int, Note>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :title || '%' AND trash = :trashed")
    fun getNotesByTitleAndAvailability(title: String?, trashed: Int): DataSource.Factory<Int, Note>

    @Query("SELECT * FROM notes WHERE _ID = :id")
    fun getNoteById(id: Int): LiveData<Note>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNote(note: Note?): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateNote(note: Note?): Int

    @Delete
    fun deleteNote(note: Note?)

    @Query("DELETE FROM notes WHERE trash = 1")
    fun deleteAll()
}