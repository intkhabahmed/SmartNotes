package com.intkhabahmed.smartnotes.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;

import com.intkhabahmed.smartnotes.R;

import java.util.Calendar;

public class ViewUtils {
    public static void showEmptyView(View recyclerView, View emptyView) {
        recyclerView.setVisibility(View.INVISIBLE);
        emptyView.setVisibility(View.VISIBLE);
    }

    public static void hideEmptyView(View recyclerView, View emptyView) {
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.INVISIBLE);
    }

    public static void showUnsavedChangesDialog(final Context context) {
        DialogInterface.OnClickListener discardButtonListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((Activity) context).finish();
                ((Activity) context).overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        };
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setPositiveButton(context.getString(R.string.discard), discardButtonListener);
        dialogBuilder.setNegativeButton(context.getString(R.string.keep_editing), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialogBuilder.setMessage(context.getString(R.string.unsaved_changes_dialog_message));
        dialogBuilder.setTitle(context.getString(R.string.unsaved_changes));
        AlertDialog alertDialog = dialogBuilder.create();
        WindowManager.LayoutParams params = alertDialog.getWindow().getAttributes();
        params.dimAmount = 0.7f;
        alertDialog.getWindow().setAttributes(params);
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        alertDialog.show();
    }

    public static void showDeleteConfirmationDialog(Context context, DialogInterface.OnClickListener deleteButtonListener, String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setPositiveButton(context.getString(R.string.yes), deleteButtonListener);
        dialogBuilder.setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialogBuilder.setMessage(message);
        dialogBuilder.setTitle(context.getString(R.string.delete_confirmation));
        AlertDialog alertDialog = dialogBuilder.create();
        WindowManager.LayoutParams params = alertDialog.getWindow().getAttributes();
        params.dimAmount = 0.7f;
        alertDialog.getWindow().setAttributes(params);
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        alertDialog.show();
    }

    public static int getColorFromAttribute(Context context, @AttrRes int attribute) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attribute, typedValue, true);
        @ColorInt int color = typedValue.data;
        return color;
    }

    public static void showDatePicker(final Context context, final DateTimeListener listener) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                listener.selectedDate(String.format("%02d-%02d-%02d", dayOfMonth, month + 1, year));
                showTimePicker(context, listener);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private static void showTimePicker(Context context, final DateTimeListener listener) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                listener.selectedTime(String.format("%02d:%02d", hourOfDay, minute));
                listener.dateTimeSelected(true);
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }
}
