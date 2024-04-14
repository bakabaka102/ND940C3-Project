package com.udacity.notify

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.udacity.DetailActivity
import com.udacity.R
import com.udacity.utils.Constants
import com.udacity.utils.Logger
import com.udacity.utils.ToastUtils
import java.util.Date

fun Context.createNotificationChannel() {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager = this.getSystemService(NotificationManager::class.java)
        val name: CharSequence = this.getString(R.string.channel_name)
        val descriptionText = this.getString(R.string.channel_description)
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

fun Context.showNotification(title: String, description: String) {
    val resultIntent = Intent(this, DetailActivity::class.java)
    // Create the TaskStackBuilder.
    val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
        // Add the intent, which inflates the back stack.
        addNextIntentWithParentStack(resultIntent)
        // Get the PendingIntent containing the entire back stack.
        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    val notification: Notification =
        NotificationCompat.Builder(this, Constants.CHANNEL_NOTIFY_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(resultPendingIntent).setAutoCancel(true)
            .build()

    if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        NotificationManagerCompat.from(this).notify(getNotificationId(), notification)
    } else {
        ToastUtils.showToast(this, "POST_NOTIFICATIONS is denied")
        Logger.d("POST_NOTIFICATIONS is denied.")
    }
}

private fun getNotificationId(): Int {
    return Date().time.toInt().also {
        Logger.d("$it")
    }
}
