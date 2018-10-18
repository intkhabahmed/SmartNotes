package com.intkhabahmed.smartnotes.utils;

import android.content.Context;
import android.os.Bundle;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.gson.Gson;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.services.NoteJobService;

public class ReminderUtils {
    synchronized public static void scheduleNoteReminder(Context context, Note note) {
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.NOTE_EXTRA, new Gson().toJson(note));

        Job noteReminderJob = jobDispatcher.newJobBuilder()
                .setService(NoteJobService.class)
                .setTag(String.format("%s_%s", note.getNoteId(), note.getDateCreated()))
                .setLifetime(Lifetime.FOREVER)
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setTrigger(Trigger.executionWindow(note.getRemainingTimeToRemind(), note.getRemainingTimeToRemind() + 60))
                .setExtras(bundle)
                .build();
        jobDispatcher.mustSchedule(noteReminderJob);
    }
}
