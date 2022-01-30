package com.example.expensesmanager2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService

class AlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        displayNotification(context!!)

    }

    private fun displayNotification(context: Context) {
        var builder = NotificationCompat.Builder(context, "NOTIFICATION")
            .setSmallIcon(R.drawable.ic_baseline_attach_money_24)
            .setContentTitle("textTitle")
            .setContentText("textContent")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "CHANNEL"
            val descriptionText = "XDDDDDDDD"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("NOTIFICATION", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager = getSystemService(context, NotificationManager::class.java) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        with(NotificationManagerCompat.from(context)) {
            notify(123, builder.build())
        }
    }
}