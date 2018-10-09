package com.intkhabahmed.smartnotes.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.ui.MainActivity;

public class NotificationUtils {

    public static void showReminderNotification(Context context, Note note) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(AppConstants.NOTIFICATION_CHANNEL_ID,
                    AppConstants.NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            if (manager != null) {
                manager.createNotificationChannel(notificationChannel);
            }
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, AppConstants.NOTIFICATION_CHANNEL_ID)
                .setContentIntent(getContentIntent(context, note))
                .setAutoCancel(true)
                .setContentTitle(note.getNoteTitle())
                .setContentText(note.getDescription())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(note.getDescription()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setPriority(ViewUtils.getColorFromAttribute(context, R.attr.colorPrimary))
                .setSmallIcon(R.drawable.ic_smart_notes)
                .setLargeIcon(largeIcon(context))
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
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
