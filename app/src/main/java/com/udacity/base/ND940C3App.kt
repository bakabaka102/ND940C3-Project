package com.udacity.base

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.udacity.R
import com.udacity.utils.Constants

class ND940C3App : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            val name: CharSequence = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            NotificationChannel(
                Constants.CHANNEL_NOTIFY_ID,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setShowBadge(true)
                description = descriptionText
                notificationManager.createNotificationChannel(this)
            }
        }
    }
}