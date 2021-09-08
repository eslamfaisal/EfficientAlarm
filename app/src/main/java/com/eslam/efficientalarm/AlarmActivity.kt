package com.eslam.efficientalarm

import android.content.Context
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.eslam.efficientalarm.AlarmData
import com.eslam.efficientalarm.Constants
import com.eslam.efficientalarm.NotificationHelper.cancelNotification
import com.eslam.efficientalarm.NotificationHelper.runNotification
import com.eslam.efficientalarm.R
import kotlinx.android.synthetic.main.activity_alarm.*
import java.util.*


class AlarmActivity : AppCompatActivity() {

    lateinit var wakeLock: PowerManager.WakeLock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                    acquire()
                }
            }


        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            (WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        )

        setContentView(R.layout.activity_alarm)

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val alarmData = intent.getParcelableExtra<AlarmData>(Constants.ALARM_DATA) as AlarmData
        alarm_title.text = alarmData.name
        runNotification(this, alarmData.name, alarmData.alarm_id)
        closeBtn.setOnClickListener {
            cancelNotification()
            finish()
        }

        val timer = Timer()
        var remainingSec = 30
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    remainingSec -= 1
                    remain_to_stop.text = remainingSec.toString()
                    if (remainingSec == 0) {
                        timer.cancel()
                        finish()
                    }

                }
            }
        }, 1000, 1000)

    }

    override fun onDestroy() {
        wakeLock.release()
        super.onDestroy()
    }


}