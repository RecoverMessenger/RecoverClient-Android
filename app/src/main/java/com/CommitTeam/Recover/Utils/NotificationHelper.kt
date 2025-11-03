package com.CommitTeam.Recover.Utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.CommitTeam.Recover.R

object NotificationHelper {

    val CHANNEL_ID = "messages"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Новые сообщения"
            val descriptionText = "Уведомления о новых сообщениях в чатах"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNewMessageNotification(context: Context, senderName: String, content: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_github)
            .setContentTitle(senderName)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(System.currentTimeMillis().toInt(), builder.build())
            } catch (e: SecurityException) {
                Log.e("NotificationHelper", "Отсутствует разрешение POST_NOTIFICATIONS", e)
            }
        }
    }
}