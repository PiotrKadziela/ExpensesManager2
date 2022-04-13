package com.example.expensesmanager2.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.expensesmanager2.R
import com.example.expensesmanager2.models.ReminderModel
import java.util.*

class AlertReceiver : BroadcastReceiver() {
    @SuppressLint("ServiceCast", "UnspecifiedImmutableFlag")
    override fun onReceive(context: Context?, intent: Intent?) {
        val period = intent!!.getIntExtra("period", 99)
        val title = intent.getStringExtra("title")
        val desc = intent.getStringExtra("desc")
        val id = intent.getIntExtra("id", 0)
        displayNotification(context!!, title!!, desc!!)
        val sql = SQLiteHelper(context)
        if(period != 99) {
            val c = Calendar.getInstance()
            when (period) {
                0 -> c.add(Calendar.WEEK_OF_YEAR, 1)
                1 -> c.add(Calendar.WEEK_OF_YEAR, 2)
                2 -> c.add(Calendar.MONTH, 1)
                3 -> c.add(Calendar.MONTH, 2)
                4 -> c.add(Calendar.MONTH, 3)
            }
            val i = Intent(context, AlertReceiver::class.java)
            i.putExtra("period", period)
            i.putExtra("title", title)
            i.putExtra("desc", desc)
            val pendingIntent = PendingIntent
                .getBroadcast(context, id, i, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
            alarmManager!!.setExact(AlarmManager.RTC, c.timeInMillis, pendingIntent)
            val reminderModel = ReminderModel(context).get("_id=$id")[0]
            reminderModel.time = c.timeInMillis
            reminderModel.update()
        } else {
            ReminderModel(context).delete("_id=$id")
        }
    }

    private fun displayNotification(context: Context, title: String, desc: String) {
        val builder = NotificationCompat.Builder(context, "NOTIFICATION")
            .setSmallIcon(R.drawable.ic_baseline_attach_money_24)
            .setContentTitle(title)
            .setContentText(desc)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "CHANNEL"
            val descriptionText = "Reminder channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("NOTIFICATION", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager = getSystemService(
                context,
                NotificationManager::class.java
            ) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        with(NotificationManagerCompat.from(context)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}