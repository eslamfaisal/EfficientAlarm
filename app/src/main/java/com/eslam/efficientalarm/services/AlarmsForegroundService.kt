package com.eslam.efficientalarm.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import com.eslam.efficientalarm.model.AlarmData
import com.eslam.efficientalarm.R
import com.eslam.efficientalarm.ui.AlarmActivity
import com.eslam.efficientalarm.utils.Constants.ALARM_DATA
import com.eslam.efficientalarm.utils.Constants.OPEN_TYPE
import com.eslam.efficientalarm.utils.Constants.REFRESH
import com.eslam.efficientalarm.utils.scheduleAlarm
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class AlarmsForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForeground(
            intent!!.getStringExtra(
                OPEN_TYPE
            )!!
        ) else startForeground(
            1,
            Notification()
        )

        if (intent!!.getStringExtra(OPEN_TYPE) == REFRESH) {
            refreshAlarms()
        } else {
            startAlarm()
        }


        return START_STICKY
    }

    private fun refreshAlarms() {
        CoroutineScope(IO).launch {
            CoroutineScope(Main).launch {
                scheduleAlarm(this@AlarmsForegroundService, getSavedAlarm())
            }
            delay(5000)
            stopForeground(true)
            stopSelf()
        }
    }

    fun getSavedAlarm(): AlarmData {
        val sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )

        return Gson().fromJson(sharedPref.getString("alarm_details", ""), AlarmData::class.java)
    }

    private fun startAlarm() {

        CoroutineScope(Main).launch {

            // get saved alarm from shared preference
            val alarm: AlarmData = getSavedAlarm()

            // schedule the alarm for next day if repeated
            if (alarm.repeated)
                scheduleAlarm(this@AlarmsForegroundService, alarm)

            val ringer = Intent(applicationContext, AlarmActivity::class.java)
            ringer.putExtra(ALARM_DATA, alarm)
            ringer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(ringer)

            delay(2000)

            stopForeground(true)
            stopSelf()
        }

    }

    private fun startForeground(type: String) {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }


        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(PRIORITY_MIN)
            .setContentTitle("المنبة")
            .setContentText(type)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(101, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }
}