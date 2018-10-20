package com.intkhabahmed.smartnotes.utils;

import android.content.Context;

import com.intkhabahmed.smartnotes.models.Note;

public class NotesReminderTasks {

    public static void executeTask(Context context, String action, Note note) {
        switch (action) {
            case AppConstants.NOTE_REMINDER_TASK:
                startNoteReminderTask(context, note);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private static void startNoteReminderTask(final Context context, Note note) {
        if (note != null) {
            NotificationUtils.showReminderNotification(context, note);
        }
    }
}
