package com.intkhabahmed.smartnotes.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.intkhabahmed.smartnotes.R;

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
        alertDialog.show();
    }
}
