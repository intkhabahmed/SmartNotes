package com.intkhabahmed.smartnotes.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChecklistItem(val title: String, var isChecked: Boolean) : Parcelable