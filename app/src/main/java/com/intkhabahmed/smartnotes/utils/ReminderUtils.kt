package com.intkhabahmed.smartnotes.utils

import android.content.Context
import android.os.Bundle
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.firebase.jobdispatcher.Lifetime
import com.firebase.jobdispatcher.RetryStrategy
import com.firebase.jobdispatcher.Trigger
import com.google.gson.Gson
import com.intkhabahmed.smartnotes.models.Note
import com.intkhabahmed.smartnotes.services.NoteJobService

object ReminderUtils {
    @Synchronized
    fun scheduleNoteReminder(context: Context?, note: Note) {
        val jobDispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
        val bundle = Bundle()
        bundle.putString(AppConstants.NOTE_EXTRA, Gson().toJson(note))
        val noteReminderJob = jobDispatcher.newJobBuilder()
                .setService(NoteJobService::class.java)
                .setTag(String.format("%s_%s", note.noteId, note.dateCreated))
                .setLifetime(Lifetime.FOREVER)
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setTrigger(Trigger.executionWindow(note.remainingTimeToRemind, note.remainingTimeToRemind + 60))
                .setExtras(bundle)
                .build()
        jobDispatcher.mustSchedule(noteReminderJob)
    }
}