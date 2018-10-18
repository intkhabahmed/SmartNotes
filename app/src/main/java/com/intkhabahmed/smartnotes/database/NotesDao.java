package com.intkhabahmed.smartnotes.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.intkhabahmed.smartnotes.models.Note;

import java.util.List;

@Dao
interface NotesDao {

    @Query("SELECT * FROM notes WHERE noteType = :type AND trash = :trashed ORDER BY " +
            "CASE :sortOrder " +
            "WHEN 'dateCreated ASC' THEN dateCreated " +
            "WHEN 'title ASC' THEN title " +
            "END ASC")
    LiveData<List<Note>> getNotesByTypeAndAvailabilityInAscendingOrder(String type, int trashed, String sortOrder);

    @Query("SELECT * FROM notes WHERE noteType = :type AND trash = :trashed ORDER BY " +
            "CASE :sortOrder " +
            "WHEN 'dateCreated DESC' THEN dateCreated " +
            "WHEN 'title DESC' THEN title " +
            "END DESC")
    LiveData<List<Note>> getNotesByTypeAndAvailabilityInDescendingOrder(String type, int trashed, String sortOrder);

    @Query("SELECT * FROM notes WHERE trash = :trashed")
    LiveData<List<Note>> getNotesByAvailability(int trashed);

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :title || '%' AND trash = :trashed")
    LiveData<List<Note>> getNotesByTitleAndAvailability(String title, int trashed);

    @Query("SELECT * FROM notes WHERE _ID = :id")
    LiveData<Note> getNoteById(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertNote(Note note);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int updateNote(Note note);

    @Delete
    void deleteNote(Note note);
}
