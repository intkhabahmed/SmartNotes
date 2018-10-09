package com.intkhabahmed.smartnotes.services;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.utils.AppConstants;
import com.intkhabahmed.smartnotes.utils.NotesReminderTasks;

public class NoteJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters job) {
        Note note = null;
        if (job.getExtras() != null) {
            note = new Gson().fromJson(job.getExtras().getString(AppConstants.NOTE_EXTRA), new TypeToken<Note>() {
            }.getType());
        }
        NotesReminderTasks.executeTask(this, AppConstants.NOTE_REMINDER_TASK, note);
        jobFinished(job, false);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}
