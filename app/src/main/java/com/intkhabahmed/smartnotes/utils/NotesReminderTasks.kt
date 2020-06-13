package com.intkhabahmed.smartnotes.utils

import android.content.Context
import com.intkhabahmed.smartnotes.models.Note
import com.intkhabahmed.smartnotes.utils.NotificationUtils.showReminderNotification

object NotesReminderTasks {
    @JvmStatic
    fun executeTask(context: Context, action: String, note: Note?) {
        if (AppConstants.NOTE_REMINDER_TASK == action) {
            startNoteReminderTask(context, note)
        } else {
            throw IllegalArgumentException()
        }
    }

    private fun startNoteReminderTask(context: Context, note: Note?) {
        note?.let { showReminderNotification(context, it) }
    }
}