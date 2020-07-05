package com.intkhabahmed.smartnotes.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.AppWidgetTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intkhabahmed.smartnotes.R
import com.intkhabahmed.smartnotes.database.NoteRepository.Companion.instance
import com.intkhabahmed.smartnotes.models.ChecklistItem
import com.intkhabahmed.smartnotes.models.Note
import com.intkhabahmed.smartnotes.services.NoteService
import com.intkhabahmed.smartnotes.ui.MainActivity
import com.intkhabahmed.smartnotes.ui.WidgetConfigureActivity
import com.intkhabahmed.smartnotes.utils.AppConstants
import com.intkhabahmed.smartnotes.utils.Global
import java.io.File

/**
 * Implementation of App Widget functionality.
 */
class NoteWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        NoteService.startActionUpdateWidget(context)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            Global.deleteDataForWidgetId(AppConstants.PREF + appWidgetId)
        }
    }

    companion object {
        private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                    appWidgetId: Int, note: Note) {
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.note_widget)
            views.setTextViewText(R.id.tv_note_title, note.noteTitle)
            var description = note.description!!
            if (note.noteType == context.getString(R.string.image_note)) {
                views.setViewVisibility(R.id.tv_note_description, View.INVISIBLE)
                views.setViewVisibility(R.id.note_image_view, View.VISIBLE)
                views.setContentDescription(R.id.note_image_view, note.noteTitle)
                val image = File(description)
                if (image.exists()) {
                    val awt: AppWidgetTarget = object : AppWidgetTarget(context, R.id.note_image_view, views, appWidgetId) {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                            super.onResourceReady(resource, transition)
                        }
                    }
                    val options = RequestOptions().override(AppConstants.WIDGET_IMAGE_SIZE, AppConstants.WIDGET_IMAGE_SIZE).placeholder(R.drawable.ic_smart_notes).error(R.drawable.ic_error_24dp)
                    Glide.with(context.applicationContext)
                            .asBitmap()
                            .load(Uri.fromFile(image))
                            .apply(options)
                            .into(awt)
                }
            } else {
                if (note.noteType == context.getString(R.string.checklist)) {
                    val builder = StringBuilder()
                    val checklistItems = Gson().fromJson<List<ChecklistItem>>(note.description, object : TypeToken<List<ChecklistItem?>?>() {}.type)
                    for ((title, isChecked) in checklistItems) {
                        builder.append(title)
                        if (isChecked) {
                            builder.append(" ").append(context.getString(R.string.checkmark_unicode))
                        }
                        builder.append("\n")
                    }
                    description = builder.toString()
                }
                views.setViewVisibility(R.id.tv_note_description, View.VISIBLE)
                views.setViewVisibility(R.id.note_image_view, View.INVISIBLE)
                views.setTextViewText(R.id.tv_note_description, description)
            }
            //setting Pending intent for starting configure activity to set the note for the widget
            val startConfigureActivityIntent = Intent(context, WidgetConfigureActivity::class.java)
            val bundle = Bundle()
            bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            startConfigureActivityIntent.putExtras(bundle)
            val pendingIntent = PendingIntent.getActivity(context, appWidgetId, startConfigureActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.note_pickup_btn, pendingIntent)

            //setting Pending intent for starting the detail page of the note
            val startDetailActivityIntent = Intent(context, MainActivity::class.java)
            startDetailActivityIntent.putExtra(AppConstants.NOTIFICATION_INTENT_EXTRA, note)
            val detailPendingIntent = PendingIntent.getActivity(context, appWidgetId, startDetailActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.tv_note_title, detailPendingIntent)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        @JvmStatic
        fun updateAllWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
            for (appWidgetId in appWidgetIds) {
                val noteLiveData = instance!!.getNoteById(
                        Global.getDataForWidgetId(AppConstants.PREF + appWidgetId))
                noteLiveData.observeForever(object : Observer<Note?> {
                    override fun onChanged(note: Note?) {
                        noteLiveData.removeObserver(this)
                        note?.let { updateAppWidget(context, appWidgetManager, appWidgetId, it) }
                    }
                })
            }
        }
    }
}