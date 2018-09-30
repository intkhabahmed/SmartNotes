package com.intkhabahmed.smartnotes.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(tableName = "notes")
public class Note implements Parcelable {
    @ColumnInfo(name = "_ID")
    @PrimaryKey(autoGenerate = true)
    private int noteId;
    @ColumnInfo(name = "title")
    private String noteTitle;
    private String description;
    private String noteType;
    private long dateCreated;
    private long dateModified;
    @ColumnInfo(name = "trash")
    private int trashed;

    @Ignore
    public Note() {
    }

    public Note(int noteId, String noteTitle, String description, String noteType, long dateCreated, long dateModified, int trashed) {
        this.noteId = noteId;
        this.noteTitle = noteTitle;
        this.description = description;
        this.noteType = noteType;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
        this.trashed = trashed;
    }

    @Ignore
    public Note(String noteTitle, String description, String noteType, long dateCreated, long dateModified, int trashed) {
        this.noteTitle = noteTitle;
        this.description = description;
        this.noteType = noteType;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
        this.trashed = trashed;
    }

    @Ignore
    protected Note(Parcel in) {
        noteId = in.readInt();
        noteTitle = in.readString();
        description = in.readString();
        noteType = in.readString();
        dateCreated = in.readLong();
        dateModified = in.readLong();
        trashed = in.readInt();
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(noteId);
        dest.writeString(noteTitle);
        dest.writeString(description);
        dest.writeString(noteType);
        dest.writeLong(dateCreated);
        dest.writeLong(dateModified);
        dest.writeInt(trashed);
    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    public int getTrashed() {
        return trashed;
    }

    public void setTrashed(int trashed) {
        this.trashed = trashed;
    }
}
