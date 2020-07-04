package com.intkhabahmed.smartnotes.utils

import android.view.WindowManager
import androidx.appcompat.app.AlertDialog

fun AlertDialog.dimWindowBackground() {
    val params = window!!.attributes
    params.dimAmount = 0.7f
    window!!.attributes = params
    window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
}