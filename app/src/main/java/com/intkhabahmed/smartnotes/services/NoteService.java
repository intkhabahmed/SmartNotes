package com.intkhabahmed.smartnotes.services;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

import com.intkhabahmed.smartnotes.widgets.NoteWidgetProvider;

public class NoteService extends JobIntentService {

    private static final String ACTION_WIDGET_UPDATE = "com.intkhabahmed.smartnotes.action.update_widget";
    private static final int JOB_ID = 1000;

    public static void startActionUpdateWidget(Context context) {
        Intent intent = new Intent(context, NoteService.class);
        intent.setAction(ACTION_WIDGET_UPDATE);
        enqueueWork(context, NoteService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@Nullable Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_WIDGET_UPDATE.equals(action)) {
                handleUpdateWidget();
            }
        }
    }

    private void handleUpdateWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] widgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, NoteWidgetProvider.class));
        NoteWidgetProvider.updateAllWidgets(this, appWidgetManager, widgetIds);
    }
}
