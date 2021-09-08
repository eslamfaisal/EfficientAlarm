package com.eslam.efficientalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.eslam.efficientalarm.Constants.alarm_id
import java.util.*
import java.util.concurrent.TimeUnit

fun scheduleAlarm(context: Context, alarm: AlarmData) {

    Log.d(TAG, "scheduleAlarm:current time ${System.currentTimeMillis()}")
    Log.d(TAG, "scheduleAlarm:current time ${alarm.time}")


    val newTime: Long
    if (System.currentTimeMillis() > alarm.time) {

        val oldCalender = Calendar.getInstance()
        oldCalender.time = Date(alarm.time)

        val newCalendar = Calendar.getInstance()
        newCalendar[Calendar.HOUR_OF_DAY] = oldCalender[Calendar.HOUR_OF_DAY]
        newCalendar[Calendar.MINUTE] = oldCalender[Calendar.MINUTE]
        newCalendar[Calendar.SECOND] = 0
        newCalendar[Calendar.MILLISECOND] = 0

        newTime = newCalendar.time.time + TimeUnit.DAYS.toMillis(1)
    } else {
        newTime = alarm.time
    }

    //Setting intent to class where Alarm broadcast message will be handled
    //Setting intent to class where Alarm broadcast message will be handled
    val intent = Intent(context, AlarmsReceiver::class.java)
    intent.putExtra(alarm_id, alarm.alarm_id)
    val alarmIntentRTC: PendingIntent =
        PendingIntent.getBroadcast(
            context,
            alarm.alarm_id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                newTime,
                alarmIntentRTC
            )

        }
        else -> {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                newTime,
                alarmIntentRTC
            )
        }
    }

    Log.d(TAG, "scheduleAlarm: once at ${newTime}")
    Log.d(TAG, "scheduleAlarm: once at ${Date(newTime)}")


    val receiver = ComponentName(context, AlarmsReceiver::class.java)
    context.packageManager.setComponentEnabledSetting(
        receiver,
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP
    )


}
