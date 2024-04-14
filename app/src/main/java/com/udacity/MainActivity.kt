package com.udacity

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.udacity.base.BaseActivity
import com.udacity.databinding.ActivityMainBinding
import com.udacity.utils.Constants
import com.udacity.utils.Logger
import com.udacity.utils.ToastUtils
import java.util.Date


class MainActivity : BaseActivity<ActivityMainBinding>() {

    private var downloadID = Constants.NO_DOWNLOAD
    private var downLoadFileName = ""

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action
    private var mContentObserver: ContentObserver? = null
    private val mDownloadManager: DownloadManager by lazy {
        getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }


    companion object {
        const val REQUEST_CODE_NOTIFICATION = 10
    }

    override fun instanceViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        setSupportActionBar(mBinding.toolbar)
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun initActions() {
        mBinding.layoutMain.btnLoading.setOnClickListener {
            downloadFile()
        }
    }

    private fun downloadFile() {
        if (mBinding.layoutMain.radioDownloadOption.checkedRadioButtonId == View.NO_ID) {
            ToastUtils.showToast(this, "Select 1 option to download")
        } else {
            downLoadFileName =
                mBinding.layoutMain.radioDownloadOption.checkedRadioButtonId.toString()
            download()
        }
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            id?.let {
                val statusQuery = statusQuery(it)
                statusQuery.takeIf { status ->
                    status != "UNKNOWN"
                }?.run {
                    showNotification(downLoadFileName, statusQuery)
                }
            }
        }
    }

    @SuppressLint("Range")
    private fun statusQuery(id: Long): String {
        val cursor = mDownloadManager.query(DownloadManager.Query().setFilterById(id))
        cursor.use {
            with(it) {
                if (moveToFirst()) {
                    return when (getInt(getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_SUCCESSFUL -> "SUCCESSFUL"
                        DownloadManager.STATUS_FAILED -> "FAILED"
                        else -> "UNKNOWN"
                    }
                }
                return "UNKNOWN"
            }
        }
    }


    private fun showNotification(title: String, description: String) {
        val notification: Notification =
            NotificationCompat.Builder(this, Constants.CHANNEL_NOTIFY_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(description)
                .setStyle(NotificationCompat.BigTextStyle().bigText(description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this@MainActivity)
                .notify(getNotificationId(), notification)
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

    private fun download() {
        //mBinding.layoutMain.btnLoading.changeStateOfButton(ButtonState.Loading)
        //Thread.sleep(2000)
        //mBinding.layoutMain.btnLoading.changeStateOfButton(ButtonState.Completed)
        downloadID.takeIf { it != Constants.NO_DOWNLOAD }
        if (downloadID != Constants.NO_DOWNLOAD) {
            mDownloadManager.remove(downloadID)
            unregisterDownloadContentObserver()
            downloadID = Constants.NO_DOWNLOAD
        }

        val request =
            DownloadManager.Request(Uri.parse(Constants.URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        // enqueue puts the download request in the queue.
        downloadID = mDownloadManager.enqueue(request)
        mDownloadManager.registerDownloadContentObserver()
    }

    private fun unregisterDownloadContentObserver() {
        mContentObserver?.let {
            contentResolver.unregisterContentObserver(it)
            mContentObserver = null
        }
    }

    private fun DownloadManager.registerDownloadContentObserver() {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                mContentObserver?.run {
                    observerDownloadProgress()
                }
            }
        }
        mContentObserver = observer
        contentResolver.registerContentObserver(
            "content://downloads/my_downloads".toUri(),
            true,
            mContentObserver as ContentObserver
        )
    }

    @SuppressLint("Range")
    private fun DownloadManager.observerDownloadProgress() {
        this.query(DownloadManager.Query().setFilterById(downloadID)).use {
            with(it) {
                if (this != null && moveToFirst()) {
                    val id = getInt(getColumnIndex(DownloadManager.COLUMN_ID))
                    when (getInt(getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_RUNNING -> {
                            Logger.d("Progress download with $id: running")
                            mBinding.layoutMain.btnLoading.changeStateOfButton(ButtonState.Loading)
                        }

                        DownloadManager.STATUS_SUCCESSFUL -> {
                            Logger.d("Progress download with $id: successful")
                            mBinding.layoutMain.btnLoading.changeStateOfButton(ButtonState.Completed)
                        }

                        DownloadManager.STATUS_FAILED -> {
                            Logger.d("Progress download with $id: failed")
                            mBinding.layoutMain.btnLoading.changeStateOfButton(ButtonState.Completed)
                        }

                        DownloadManager.STATUS_PAUSED -> {
                            Logger.d("Progress download with $id: paused")
                        }

                        DownloadManager.STATUS_PENDING -> {
                            Logger.d("Progress download with $id: pending")
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        ToastUtils.cancelToast()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        unregisterDownloadContentObserver()
    }
}