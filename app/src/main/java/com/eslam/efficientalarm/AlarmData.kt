package com.eslam.efficientalarm

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class AlarmData(
    val alarm_id: Int,
    var time: Long,
    var name: String,
    val repeated: Boolean,
) : Parcelable