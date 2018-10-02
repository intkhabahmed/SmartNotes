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

    @Query("SELECT * FROM notes WHERE noteType = :type AND trash = :trashed ORDER BY :sortOrder")
    LiveData<List<Note>> getNotesByTypeAndAvailability(String type, int trashed, String sortOrder);

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :title || '%' AND trash = :trashed")
    LiveData<List<Note>> getNotesByTitleAndAvailability(String title, int trashed);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertNote(Note note);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int updateNote(Note note);

    @Delete
    int deleteNote(Note note);
}
