package com.intkhabahmed.smartnotes.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intkhabahmed.smartnotes.R
import com.intkhabahmed.smartnotes.models.ChecklistItem
import com.intkhabahmed.smartnotes.models.Note
import com.intkhabahmed.smartnotes.ui.MainActivity

internal object NotificationUtils {
    @JvmStatic
    fun showReminderNotification(context: Context, note: Note) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(AppConstants.NOTIFICATION_CHANNEL_ID,
                    AppConstants.NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(notificationChannel)
        }
        var noteDescription = note.description
        if (note.noteType == context.getString(R.string.checklist)) {
            val tasks = StringBuilder()
            val checklistItems = Gson().fromJson<List<ChecklistItem>>(note.description, object : TypeToken<List<ChecklistItem?>?>() {}.type)
            for ((title) in checklistItems) {
                tasks.append(title)
                tasks.append("\n")
            }
            noteDescription = tasks.toString()
        } else if (note.noteType == context.getString(R.string.image_note)) {
            noteDescription = ""//context.getString(R.string.image_note_notification_message)
        }
        val notificationBuilder = NotificationCompat.Builder(context, AppConstants.NOTIFICATION_CHANNEL_ID)
                .setContentIntent(getContentIntent(context, note))
                .setAutoCancel(true)
                .setContentTitle(note.noteTitle)
                .setContentText(noteDescription)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_smart_notes)
                .setLargeIcon(largeIcon(context))
                .setStyle(NotificationCompat.BigTextStyle().bigText(noteDescription))
                .setColor(Color.rgb(0, 100, 0))
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.priority = NotificationCompat.PRIORITY_HIGH
        }
        if (note.noteType == context.getString(R.string.image_note)) {
            Glide.with(context).asBitmap().load(note.description).into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {

                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    notificationBuilder.apply {
                        setContentText("")
                        setStyle(NotificationCompat.BigPictureStyle(notificationBuilder).bigPicture(resource))
                    }
                    manager.notify(note.noteId, notificationBuilder.build())
                }
            })
        } else {
            manager.notify(note.noteId, notificationBuilder.build())
        }
    }

    private fun getContentIntent(context: Context, note: Note): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(AppConstants.NOTIFICATION_INTENT_EXTRA, note)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun largeIcon(context: Context): Bitmap? {
        val res = context.resources
        return BitmapFactory.decodeResource(res, R.drawable.ic_smart_notes_24px)
    }
}