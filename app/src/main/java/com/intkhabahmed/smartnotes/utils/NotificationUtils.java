package com.intkhabahmed.smartnotes.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.models.ChecklistItem;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.ui.MainActivity;

import java.util.List;

class NotificationUtils {

    static void showReminderNotification(final Context context, final Note note) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(AppConstants.NOTIFICATION_CHANNEL_ID,
                    AppConstants.NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            if (manager != null) {
                manager.createNotificationChannel(notificationChannel);
            }
        }
        String noteDescription = note.getDescription();
        if (note.getNoteType().equals(context.getString(R.string.checklist))) {
            StringBuilder tasks = new StringBuilder();
            List<ChecklistItem> checklistItems = new Gson().fromJson(note.getDescription(), new TypeToken<List<ChecklistItem>>() {
            }.getType());
            for (ChecklistItem item : checklistItems) {
                tasks.append(item.getTitle());
                tasks.append("\n");
            }
            noteDescription = tasks.toString();
        } else if (note.getNoteType().equals(context.getString(R.string.image_note))) {
            noteDescription = context.getString(R.string.image_note_notification_message);
        }

        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, AppConstants.NOTIFICATION_CHANNEL_ID)
                .setContentIntent(getContentIntent(context, note))
                .setAutoCancel(true)
                .setContentTitle(note.getNoteTitle())
                .setContentText(noteDescription)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_smart_notes)
                .setLargeIcon(largeIcon(context))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(noteDescription))
                .setColor(Color.rgb(0, 100, 0))
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        if (manager != null) {
            manager.notify(note.getNoteId(), notificationBuilder.build());
        }
    }

    private static PendingIntent getContentIntent(Context context, Note note) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(AppConstants.NOTIFICATION_INTENT_EXTRA, note);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();
        return BitmapFactory.decodeResource(res, R.drawable.ic_smart_notes_24px);
    }
}
