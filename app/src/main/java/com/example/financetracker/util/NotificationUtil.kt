package com.example.financetracker.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.financetracker.MainActivity
import com.example.financetracker.R

class NotificationUtil(private val context: Context) {
    companion object {
        private const val CHANNEL_ID = "finance_tracker_channel"
        private const val BUDGET_WARNING_NOTIFICATION_ID = 1
        private const val BUDGET_EXCEEDED_NOTIFICATION_ID = 2
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Finance Tracker Notifications"
            val descriptionText = "Notifications for budget alerts"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showBudgetWarningNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dashboard)
            .setContentTitle(context.getString(R.string.budget_warning_title))
            .setContentText(context.getString(R.string.budget_warning_message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(BUDGET_WARNING_NOTIFICATION_ID, builder.build())
            } catch (e: SecurityException) {
                // Handle notification permission not granted
                e.printStackTrace()
            }
        }
    }

    fun showBudgetExceededNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dashboard)
            .setContentTitle(context.getString(R.string.budget_exceeded_title))
            .setContentText(context.getString(R.string.budget_exceeded_message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(BUDGET_EXCEEDED_NOTIFICATION_ID, builder.build())
            } catch (e: SecurityException) {
                // Handle notification permission not granted
                e.printStackTrace()
            }
        }
    }
}
