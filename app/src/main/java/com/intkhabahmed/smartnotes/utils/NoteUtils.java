package com.intkhabahmed.smartnotes.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.v4.app.ShareCompat;
import android.text.format.DateUtils;

import com.intkhabahmed.smartnotes.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by INTKHAB on 25-03-2018.
 */

public class NoteUtils {

    public static String getFormattedTime(long timeInMillis, long currentTime) {
        return DateUtils.getRelativeTimeSpanString(timeInMillis, currentTime, DateUtils.MINUTE_IN_MILLIS).toString();
    }

    public static String getFormattedTime(long timeInMillis) {
        DateFormat formatter = SimpleDateFormat.getDateInstance();
        return formatter.format(new Date(timeInMillis));
    }

    public static int getRelativeTimeFromNow(String dateTimeString) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        try {
            Date date = formatter.parse(dateTimeString);
            return (int) (date.getTime() - System.currentTimeMillis()) / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void shareNote(Context context, String shareText) {
        ShareCompat.IntentBuilder.from((Activity) context)
                .setType(context.getString(R.string.share_type_text))
                .setText(shareText)
                .setChooserTitle(context.getString(R.string.chooser_title))
                .startChooser();
    }
}
