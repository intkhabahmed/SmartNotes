package com.intkhabahmed.smartnotes.database;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.intkhabahmed.smartnotes.models.Note;

@Dao
interface NotesDao {

    @Query("SELECT * FROM notes WHERE noteType = :type AND trash = :trashed ORDER BY " +
            "CASE :sortOrder " +
            "WHEN 'dateCreated ASC' THEN dateCreated " +
            "WHEN 'title ASC' THEN title " +
            "END ASC")
    DataSource.Factory<Integer, Note> getNotesByTypeAndAvailabilityInAscendingOrder(String type, int trashed, String sortOrder);

    @Query("SELECT * FROM notes WHERE noteType = :type AND trash = :trashed ORDER BY " +
            "CASE :sortOrder " +
            "WHEN 'dateCreated DESC' THEN dateCreated " +
            "WHEN 'title DESC' THEN title " +
            "END DESC")
    DataSource.Factory<Integer, Note> getNotesByTypeAndAvailabilityInDescendingOrder(String type, int trashed, String sortOrder);

    @Query("SELECT * FROM notes WHERE trash = :trashed")
    DataSource.Factory<Integer, Note> getNotesByAvailability(int trashed);

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :title || '%' AND trash = :trashed")
    DataSource.Factory<Integer, Note> getNotesByTitleAndAvailability(String title, int trashed);

    @Query("SELECT * FROM notes WHERE _ID = :id")
    LiveData<Note> getNoteById(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertNote(Note note);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int updateNote(Note note);

    @Delete
    void deleteNote(Note note);

    @Query("DELETE FROM notes WHERE trash = 1")
    void deleteAll();
}
