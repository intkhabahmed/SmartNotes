package com.intkhabahmed.smartnotes.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.models.ChecklistItem;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.services.NoteService;
import com.intkhabahmed.smartnotes.ui.WidgetConfigureActivity;
import com.intkhabahmed.smartnotes.utils.AppConstants;
import com.intkhabahmed.smartnotes.utils.Global;

import java.io.File;
import java.util.List;

/**
 * Implementation of App Widget functionality.
 */
public class NoteWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(final Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Note note) {
        // Construct the RemoteViews object
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.note_widget);
        views.setTextViewText(R.id.tv_note_title, note.getNoteTitle());
        String description = note.getDescription();
        if (note.getNoteType().equals(context.getString(R.string.checklist))) {
            StringBuilder builder = new StringBuilder();
            List<ChecklistItem> checklistItems = new Gson().fromJson(note.getDescription(), new TypeToken<List<ChecklistItem>>() {
            }.getType());
            for (ChecklistItem item : checklistItems) {
                builder.append(item.getTitle());
                if (item.isChecked()) {
                    builder.append(" ").append(context.getString(R.string.checkmark_unicode));
                }
                builder.append("\n");
            }
            description = builder.toString();
        } else if (note.getNoteType().equals(context.getString(R.string.image_note))) {
            views.setViewVisibility(R.id.tv_note_description, View.INVISIBLE);
            views.setViewVisibility(R.id.note_image_view, View.VISIBLE);
            final File image = new File(note.getDescription());
            if (image.exists()) {
                AppWidgetTarget awt = new AppWidgetTarget(context, R.id.note_image_view, views, appWidgetId) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        super.onResourceReady(resource, transition);
                    }
                };

                RequestOptions options = new RequestOptions().
                        override(AppConstants.WIDGET_IMAGE_SIZE, AppConstants.WIDGET_IMAGE_SIZE).placeholder(R.drawable.ic_smart_notes).error(R.drawable.ic_error_24dp);

                Glide.with(context.getApplicationContext())
                        .asBitmap()
                        .load(Uri.fromFile(image))
                        .apply(options)
                        .into(awt);
            }
        } else {
            views.setViewVisibility(R.id.tv_note_description, View.VISIBLE);
            views.setViewVisibility(R.id.note_image_view, View.INVISIBLE);
            views.setTextViewText(R.id.tv_note_description, description);
        }
        Intent intent = new Intent(context, WidgetConfigureActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.note_pickup_btn, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        NoteService.startActionUpdateWidget(context);
    }

    public static void updateAllWidgets(final Context context, final AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (final int appWidgetId : appWidgetIds) {
            final LiveData<Note> noteLiveData = NoteRepository.getInstance().getNoteById(
                    Global.getDataForWidgetId(AppConstants.PREF + appWidgetId));
            noteLiveData.observeForever(new Observer<Note>() {
                @Override
                public void onChanged(@Nullable Note note) {
                    noteLiveData.removeObserver(this);
                    if (note != null) {
                        updateAppWidget(context, appWidgetManager, appWidgetId, note);
                    }
                }
            });
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            Global.deleteDataForWidgetId(AppConstants.PREF + appWidgetId);
        }
    }
}

