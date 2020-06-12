package com.intkhabahmed.smartnotes.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
        @ColumnInfo(name = "_ID")
        @PrimaryKey(autoGenerate = true)
        var noteId: Int = 0,
        @ColumnInfo(name = "title")
        var noteTitle: String?,
        var description: String?,
        var noteType: String?,
        var dateCreated: Long,
        var dateModified: Long = 0,
        @ColumnInfo(name = "trash")
        var trashed: Int = 0,
        var remainingTimeToRemind: Int = 0,
        var reminderDateTime: String? = null) : Parcelable {


    @Ignore
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readLong(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString())

    @Ignore
    constructor() : this(0, "", "", "", 0, 0, 0, 0, "")

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(noteId)
        dest.writeString(noteTitle)
        dest.writeString(description)
        dest.writeString(noteType)
        dest.writeLong(dateCreated)
        dest.writeLong(dateModified)
        dest.writeInt(trashed)
        dest.writeInt(remainingTimeToRemind)
        dest.writeString(reminderDateTime)
    }

    companion object CREATOR : Parcelable.Creator<Note> {
        override fun createFromParcel(parcel: Parcel): Note {
            return Note(parcel)
        }

        override fun newArray(size: Int): Array<Note?> {
            return arrayOfNulls(size)
        }
    }
}
