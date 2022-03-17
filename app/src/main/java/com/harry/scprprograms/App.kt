package com.harry.scprprograms

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.harry.scprprograms.util.Constants.CHANNEL_1_NAME
import com.harry.scprprograms.util.Constants.CHANNEL_2_NAME
import com.harry.scprprograms.util.Constants.CHANNEL_ID_1
import com.harry.scprprograms.util.Constants.CHANNEL_ID_2

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel1 = NotificationChannel(
                CHANNEL_ID_1,
                CHANNEL_1_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel1.apply {
                description = ("Channel 1 Description")
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
            val notificationChannel2 = NotificationChannel(
                CHANNEL_ID_2,
                CHANNEL_2_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel2.apply {
                description = ("Channel 2 Description")
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel1)
            notificationManager.createNotificationChannel(notificationChannel2)
        }
    }
}