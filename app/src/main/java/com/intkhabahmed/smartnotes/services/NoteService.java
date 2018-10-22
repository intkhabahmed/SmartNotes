package com.intkhabahmed.smartnotes.services;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.intkhabahmed.smartnotes.widgets.NoteWidgetProvider;

public class NoteService extends IntentService {

    private static final String ACTION_WIDGET_UPDATE = "com.intkhabahmed.smartnotes.action.update_widget";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public NoteService() {
        super(NoteService.class.getSimpleName());
    }

    public static void startActionUpdateWidget(Context context) {
        Intent intent = new Intent(context, NoteService.class);
        intent.setAction(ACTION_WIDGET_UPDATE);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
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
