package com.eslam.efficientalarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.text.format.Time
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import com.eslam.efficientalarm.Constants.ALARM_DATA
import com.eslam.efficientalarm.Constants.OPEN_TYPE
import com.eslam.efficientalarm.Constants.REFRESH
import com.eslam.efficientalarm.Constants.alarm_id
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit


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
            startAlarm(intent.getIntExtra(alarm_id, -1))
        }


        return START_STICKY
    }

    private fun refreshAlarms() {
        CoroutineScope(IO).launch {
            CoroutineScope(Main).launch {

            }
            delay(5000)
            stopForeground(true)
            stopSelf()
        }
    }

    private fun updateAlarm(time: Long, alarm_id: Int) {

        var newTime = time


        newTime + TimeUnit.DAYS.toMillis(1)

        CoroutineScope(IO).launch {

            delay(500)
        }

    }

    fun getTimeMillsFromCalender(time: Time): Calendar {
        val calender = Calendar.getInstance()
        calender[Calendar.HOUR_OF_DAY] = time.hour
        calender[Calendar.MINUTE] = time.minute
        calender[Calendar.SECOND] = 0
        calender[Calendar.MILLISECOND] = 0

        return calender
    }

    private fun startAlarm(alarmId: Int) {

        CoroutineScope(Main).launch {


            val sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE
            )

            var alarm: AlarmData =
                Gson().fromJson(sharedPref.getString("alarm_details",""), AlarmData::class.java)

            updateAlarm(
                alarm_id = alarmId,
                time = alarm.time
            )
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