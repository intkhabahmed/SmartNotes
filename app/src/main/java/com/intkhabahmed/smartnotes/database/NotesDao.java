package com.intkhabahmed.smartnotes.database;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

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
