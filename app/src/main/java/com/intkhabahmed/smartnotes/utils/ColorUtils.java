package com.intkhabahmed.smartnotes.utils;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.util.TypedValue;

public class ColorUtils {
    public static int getColor(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        @AttrRes int attrRes = attr;
        theme.resolveAttribute(attrRes, typedValue, true);
        @ColorInt int color = typedValue.data;
        return color;
    }
}
