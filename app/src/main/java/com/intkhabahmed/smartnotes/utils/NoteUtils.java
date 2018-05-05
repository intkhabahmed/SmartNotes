package com.intkhabahmed.smartnotes.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ShareCompat;
import android.text.format.DateUtils;

import com.intkhabahmed.smartnotes.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by INTKHAB on 25-03-2018.
 */

public class NoteUtils {

    public static String getFormattedTime(long timeInMillis, long currentTime) {
        return DateUtils.getRelativeTimeSpanString(timeInMillis, currentTime, DateUtils.MINUTE_IN_MILLIS).toString();
    }

    public static String getFormattedTime(long timeInMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/YYYY", Locale.US);
        return formatter.format(new Date(timeInMillis));
    }

    public static void shareNote(Context context, String shareText) {
        ShareCompat.IntentBuilder.from((Activity) context)
                .setType(context.getString(R.string.share_type_text))
                .setText(shareText)
                .setChooserTitle(context.getString(R.string.chooser_title))
                .startChooser();
    }
}
