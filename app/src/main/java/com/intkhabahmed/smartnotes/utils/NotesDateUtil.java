package com.intkhabahmed.smartnotes.utils;

import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by INTKHAB on 25-03-2018.
 */

public class NotesDateUtil {

    public static String getFormattedTime(long timeInMillis, long currentTime){
        return DateUtils.getRelativeTimeSpanString(timeInMillis, currentTime, DateUtils.MINUTE_IN_MILLIS).toString();
    }

    public static String getFormattedTime(long timeInMillis){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/YYYY", Locale.US);
        return formatter.format(new Date(timeInMillis));
    }
}
