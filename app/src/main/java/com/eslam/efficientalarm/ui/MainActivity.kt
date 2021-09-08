package com.eslam.efficientalarm.ui

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.eslam.efficientalarm.model.AlarmData
import com.eslam.efficientalarm.R
import com.eslam.efficientalarm.listeners.OnDateSet
import com.eslam.efficientalarm.listeners.OnTimeSet
import com.eslam.efficientalarm.utils.scheduleAlarm
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    var hour: Int = 0
    var minute: Int = 0
    var year: Int = 0
    var month: Int = 0
    var day: Int = 0

    @RequiresApi(Build.VERSION_CODES.M)
    val resultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            Log.d(TAG, "Request permission: " + result.resultCode)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setAlarmButton.setOnClickListener {
            setAlarm()
        }
        //show dialog for request work in background and over lay
        requestAlertPermission()
    }

    fun showDatePickerDialog(v: View) {
        DatePickerFragment(object : OnDateSet {
            override fun pickedDate(year: Int, month: Int, day: Int) {
                this@MainActivity.day = day
                this@MainActivity.month = month
                this@MainActivity.year = year
            }
        }).show(supportFragmentManager, "datePicker")
    }

    fun showTimePickerDialog(v: View) {
        TimePickerFragment(object : OnTimeSet {
            override fun pickedTime(hoursOfDay: Int, minutes: Int) {
                this@MainActivity.hour = hoursOfDay
                this@MainActivity.minute = minutes
            }
        }).show(supportFragmentManager, "timePicker")
    }

    private fun setAlarm() {

        if (hour == 0 || minute == 0 || year == 0 || month == 0 || day == 0) {
            Toast.makeText(this, "pick date and time yalaa", Toast.LENGTH_LONG).show()
            return
        }

        val newCalendar = Calendar.getInstance()
        newCalendar[Calendar.YEAR] = year
        newCalendar[Calendar.MONTH] = month
        newCalendar[Calendar.DAY_OF_MONTH] = day
        newCalendar[Calendar.HOUR_OF_DAY] = hour
        newCalendar[Calendar.MINUTE] = minute
        newCalendar[Calendar.SECOND] = 0
        newCalendar[Calendar.MILLISECOND] = 0

        val alarm = AlarmData(
            alarm_id = 52,
            time = newCalendar.time.time,
            repeated = isRepeatedCheckBox.isChecked,
            name = alarmNameEdiText.text.toString()
        )

        val sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )

        sharedPref.edit().putString("alarm_details", Gson().toJson(alarm)).commit()

        scheduleAlarm(this, alarm)
    }

    fun requestAlertPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // check if we already  have permission to draw over other apps
            if (!Settings.canDrawOverlays(this)) {
                val alert = AlertDialog.Builder(this)
                alert.setTitle("Play In background")
                alert.setMessage("Please allow the app to run in the background so the app functions work properly.")
                alert.setPositiveButton("Ok") { _: DialogInterface?, _: Int ->
                    requestDrawOverlay()
                }
                alert.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> finish() }
                alert.show()
            } else {
                requestBatteryOptimizationPermission()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestDrawOverlay() {
        // if not construct intent to request permission
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + applicationContext.packageName)
        )
        resultLauncher.launch(intent)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun requestBatteryOptimizationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:" + this.applicationContext.packageName)
            checkIntentAndStart(this, intent)
        } else {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            if (checkIntentAndStart(this, intent))
                Toast.makeText(
                    this,
                    "Please enable battery optimizations switch",
                    Toast.LENGTH_LONG
                ).show()
        }
    }


    private fun checkIntentAndStart(context: Context, intent: Intent): Boolean {
        intent.resolveActivity(context.packageManager)?.let {
            context.startActivity(intent)
            return true
        }

        return false
    }


}


