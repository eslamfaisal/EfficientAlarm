package com.eslam.efficientalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.eslam.efficientalarm.Constants.BOOT_COMPLETED
import com.eslam.efficientalarm.Constants.FIRE_ALARM
import com.eslam.efficientalarm.Constants.OPEN_TYPE
import com.eslam.efficientalarm.Constants.REFRESH
import com.eslam.efficientalarm.Constants.alarm_id

class AlarmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val newIntent = Intent(context, AlarmsForegroundService::class.java)

        if (intent.action == BOOT_COMPLETED) {
            newIntent.putExtra(OPEN_TYPE, REFRESH)
        } else {
            val alarmid = intent.getIntExtra(alarm_id, -1)
            if (alarmid == -1) return
            newIntent.putExtra(OPEN_TYPE, FIRE_ALARM)
            newIntent.putExtra(alarm_id, alarmid)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(newIntent)
        } else {
            context.startService(newIntent)
        }
    }


}
