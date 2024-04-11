package com.udacity

import android.Manifest
import android.app.DownloadManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.udacity.base.BaseActivity
import com.udacity.databinding.ActivityMainBinding
import com.udacity.utils.Constants
import com.udacity.utils.Logger
import java.util.Date


class MainActivity : BaseActivity<ActivityMainBinding>() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action
    private var mToast: Toast? = null

    override fun instanceViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        setSupportActionBar(mBinding.toolbar)
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun initActions() {
        mBinding.layoutMain.btnLoading.setOnClickListener {
            showNotification()
            mBinding.layoutMain.btnLoading.changeStateOfButton(ButtonState.Loading)
            //Thread.sleep(2000)
            mBinding.layoutMain.btnLoading.changeStateOfButton(ButtonState.Completed)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        }
    }

    private fun showToast(text: String?) {
        mToast?.cancel()
        mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
        //mToast?.setText(text)
        mToast?.show()
    }

    private fun showNotification() {
        val notification: Notification =
            NotificationCompat.Builder(this, Constants.CHANNEL_NOTIFY_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("longTitle1")
                .setContentText("longText1")
                .setStyle(NotificationCompat.BigTextStyle().bigText("longText1"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this@MainActivity)
                .notify(getNotificationId(), notification)
        } else {
            showToast("POST_NOTIFICATIONS is denied")
            Logger.d("POST_NOTIFICATIONS is denied.")
        }
    }

    private fun getNotificationId(): Int {
        return Date().time.toInt().also {
            Logger.d("$it")
        }
    }

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(Constants.URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    override fun onPause() {
        super.onPause()
        mToast?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}