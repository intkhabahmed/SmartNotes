package com.intkhabahmed.smartnotes.utils

import android.app.Activity
import android.content.Context
import android.text.format.DateUtils
import androidx.core.app.ShareCompat
import com.intkhabahmed.smartnotes.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by INTKHAB on 25-03-2018.
 */
object NoteUtils {

    fun getFormattedTime(timeInMillis: Long, currentTime: Long): String {
        return DateUtils.getRelativeTimeSpanString(timeInMillis, currentTime, DateUtils.MINUTE_IN_MILLIS).toString()
    }

    @JvmStatic
    fun getFormattedTime(timeInMillis: Long): String {
        val formatter = SimpleDateFormat.getDateInstance()
        return formatter.format(Date(timeInMillis))
    }

    @JvmStatic
    fun getRelativeTimeFromNow(dateTimeString: String?): Int {
        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        return try {
            val date = formatter.parse(dateTimeString ?: "")
            (date?.time?.minus(System.currentTimeMillis()))!!.toInt() / 1000
        } catch (e: ParseException) {
            e.printStackTrace()
            0
        }
    }

    @JvmStatic
    fun shareNote(context: Context, shareText: String?) {
        ShareCompat.IntentBuilder.from(context as Activity)
                .setType(context.getString(R.string.share_type_text))
                .setText(shareText)
                .setChooserTitle(context.getString(R.string.chooser_title))
                .startChooser()
    }
}