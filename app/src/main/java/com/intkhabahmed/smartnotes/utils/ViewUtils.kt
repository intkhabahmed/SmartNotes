package com.intkhabahmed.smartnotes.utils

import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.DialogInterface
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.appcompat.app.AlertDialog
import com.intkhabahmed.smartnotes.R
import java.util.*

object ViewUtils {
    @JvmStatic
    fun showEmptyView(recyclerView: View, emptyView: View) {
        recyclerView.visibility = View.INVISIBLE
        emptyView.visibility = View.VISIBLE
    }

    @JvmStatic
    fun hideEmptyView(recyclerView: View, emptyView: View) {
        recyclerView.visibility = View.VISIBLE
        emptyView.visibility = View.INVISIBLE
    }

    fun showUnsavedChangesDialog(context: Context) {
        val discardButtonListener = DialogInterface.OnClickListener { _, _ ->
            (context as Activity).finish()
            context.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        val dialogBuilder = AlertDialog.Builder(context).apply {
            setPositiveButton(context.getString(R.string.discard), discardButtonListener)
            setNegativeButton(context.getString(R.string.keep_editing)) { dialogInterface, i -> dialogInterface.dismiss() }
            setMessage(context.getString(R.string.unsaved_changes_dialog_message))
            setTitle(context.getString(R.string.unsaved_changes))
        }

        with(dialogBuilder.create()) {
            dimWindowBackground()
            show()
        }
    }

    fun showDeleteConfirmationDialog(context: Context, deleteButtonListener: DialogInterface.OnClickListener?, message: String?) {
        val dialogBuilder = AlertDialog.Builder(context).apply {
            setPositiveButton(context.getString(R.string.yes), deleteButtonListener)
            setNegativeButton(context.getString(R.string.no)) { dialogInterface, i -> dialogInterface.dismiss() }
            setMessage(message)
            setTitle(context.getString(R.string.delete_confirmation))
        }

        with(dialogBuilder.create()) {
            dimWindowBackground()
            show()
        }
    }

    fun getColorFromAttribute(context: Context, @AttrRes attribute: Int): Int {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(attribute, typedValue, true)
        return typedValue.data
    }

    fun showDatePicker(context: Context, listener: DateTimeListener) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(context, OnDateSetListener { _, year, month, dayOfMonth ->
            listener.selectedDate(String.format("%02d-%02d-%02d", dayOfMonth, month + 1, year))
            showTimePicker(context, listener)
        }, calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH])
        datePickerDialog.show()
    }

    private fun showTimePicker(context: Context, listener: DateTimeListener) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(context, OnTimeSetListener { _, hourOfDay, minute ->
            listener.selectedTime(String.format("%02d:%02d", hourOfDay, minute))
            listener.dateTimeSelected(true)
        }, calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE], true)
        timePickerDialog.show()
    }
}