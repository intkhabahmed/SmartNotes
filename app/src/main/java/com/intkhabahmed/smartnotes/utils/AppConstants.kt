package com.intkhabahmed.smartnotes.utils

interface AppConstants {
    companion object {
        const val SORT_CRITERIA = "Sort Criteria"
        const val SORT_CRITERIA_ID = "Sort Id"
        const val SORT_CRITERIA_ID_DEFAULT = 4
        const val COLUMN_DATE_CREATED_ASC = "dateCreated ASC"
        const val COLUMN_DATE_CREATED_DESC = "dateCreated DESC"
        const val COLUMN_TITLE_ASC = "title ASC"
        const val COLUMN_TITLE_DESC = "title DESC"
        const val ASC = "ASC"
        const val NOTE_REMINDER_TASK = "note_reminder_task"
        const val NOTIFICATION_CHANNEL_ID = "reminder_notification_channel"
        const val NOTIFICATION_CHANNEL_NAME: String = "Reminder Notifications"
        const val NOTIFICATION_INTENT_EXTRA = "notification_intent_extra"
        const val NOTE_EXTRA = "job_note_extra"
        const val PREF = "pref_"
        const val WIDGET_IMAGE_SIZE = 1000
        const val INVALID_VIEW_TYPE = "Unknown ViewType"
        const val APP_OPEN_TIME = "app_open_time"
    }
}