package com.intkhabahmed.smartnotes.utils;

import android.view.View;

public class ViewUtils {
    public static void showEmptyView(View recyclerView, View emptyView) {
        recyclerView.setVisibility(View.INVISIBLE);
        emptyView.setVisibility(View.VISIBLE);
    }

    public static void hideEmptyView(View recyclerView, View emptyView) {
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.INVISIBLE);
    }
}
