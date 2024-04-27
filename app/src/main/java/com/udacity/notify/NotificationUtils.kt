package com.udacity.notify

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.udacity.DetailActivity
import com.udacity.R
import com.udacity.utils.Constants
import com.udacity.utils.Logger
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

@SuppressLint("MissingPermission")
fun Context.showNotification(title: String, description: String) {
    Logger.d("title: $title  --- description: $description")
    val resultIntent = Intent(this, DetailActivity::class.java).apply {
        putExtras(
            bundleOf(
                Constants.FILE_NAME to title,
                Constants.DOWNLOAD_STATUS to description
            )
        )
    }
    val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
        addNextIntentWithParentStack(resultIntent)
        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
    val statusAction = NotificationCompat.Action.Builder(
        R.mipmap.ic_launcher,
        this.getString(R.string.notification_action_status),
        resultPendingIntent
    ).build()
    val notification: Notification =
        NotificationCompat.Builder(this, Constants.CHANNEL_NOTIFY_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(resultPendingIntent).setAutoCancel(true)
            .addAction(statusAction)
            .build()

    if (this.isPermissionsGranted(Manifest.permission.POST_NOTIFICATIONS)) {
        NotificationManagerCompat.from(this).notify(getNotificationId(), notification)
    }
}

fun Context.isPermissionsGranted(vararg permissions: String): Boolean {
    permissions.forEach { permission ->
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
    }
    return true
}

private fun getNotificationId(): Int {
    return Date().time.toInt().also {
        Logger.d("$it")
    }
}
