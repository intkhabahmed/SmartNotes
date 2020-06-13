package com.intkhabahmed.smartnotes.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.intkhabahmed.smartnotes.models.Note

@Database(entities = [Note::class], version = 1)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDao

    companion object {
        private val LOCK = Any()
        private var sInstance: NotesDatabase? = null
        private const val DATABASE_NAME = "smartNotes.db"
        @JvmStatic
        fun getInstance(context: Context?): NotesDatabase? {
            if (sInstance == null) {
                synchronized(LOCK) {
                    sInstance = Room.databaseBuilder(context!!, NotesDatabase::class.java, DATABASE_NAME)
                            .build()
                }
            }
            return sInstance
        }
    }
}