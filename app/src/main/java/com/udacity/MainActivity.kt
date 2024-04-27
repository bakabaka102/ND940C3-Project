package com.udacity

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.View
import android.widget.RadioButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.udacity.base.BaseActivity
import com.udacity.databinding.ActivityMainBinding
import com.udacity.download.DownloadStatus
import com.udacity.download.requestDownload
import com.udacity.download.statusQuery
import com.udacity.notify.createNotificationChannel
import com.udacity.notify.isPermissionsGranted
import com.udacity.notify.showNotification
import com.udacity.utils.Constants
import com.udacity.utils.Logger
import com.udacity.utils.NetWorkUtils
import com.udacity.utils.ToastUtils

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private var downloadID = Constants.NO_DOWNLOAD
    private var downLoadFileName = ""
    private var mContentObserver: ContentObserver? = null
    private val mDownloadManager: DownloadManager by lazy {
        getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }
    private val permissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var allGranted = true
            for (value in result.values) {
                allGranted = allGranted && value
            }
            if (!allGranted) {
                //showSettingDialog()
            }
        }

    companion object {
        const val REQUEST_CODE_NOTIFICATION = 101
    }

    override fun instanceViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (this.isPermissionsGranted(Manifest.permission.POST_NOTIFICATIONS)) {
                /*ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_NOTIFICATION
                )*/
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_NOTIFICATION
                )
            }
        }
        setSupportActionBar(mBinding.toolbar)
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_NOTIFICATION) {
            if (grantResults.getOrNull(0) == PackageManager.PERMISSION_GRANTED) {
                ToastUtils.showToast(this, "Allowed")
            } else {
                ToastUtils.showToast(this, "Denied")
            }
        }
    }

    override fun initActions() {
        mBinding.layoutMain.btnLoading.setOnClickListener {
            if (NetWorkUtils.isConnected(this)) {
                downloadFile()
            } else {
                ToastUtils.showToast(this, "No internet connected")
            }
        }
    }

    private fun showSettingDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Notification Permission")
            .setMessage("Notification permission is required, Please allow notification permission from setting")
            .setPositiveButton("Ok") { _, _ ->
                val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun downloadFile() {
        if (mBinding.layoutMain.radioDownloadOption.checkedRadioButtonId == View.NO_ID) {
            ToastUtils.showToast(this, "Select 1 option to download")
        } else {
            val checkedID = mBinding.layoutMain.radioDownloadOption.checkedRadioButtonId
            downLoadFileName = mBinding.root.findViewById<RadioButton>(checkedID).text.toString()
            download()
        }
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            id?.let {
                val statusQuery = mDownloadManager.statusQuery(it)
                if (statusQuery != DownloadStatus.Unknown) {
                    this@MainActivity.createNotificationChannel()
                    this@MainActivity.showNotification(downLoadFileName, statusQuery.name)
                    ToastUtils.showToast(this@MainActivity, "Download ${statusQuery.name}")
                }
            }
        }
    }

    private fun download() {
        if (downloadID != Constants.NO_DOWNLOAD) {
            mDownloadManager.remove(downloadID)
            unregisterDownloadContentObserver()
            downloadID = Constants.NO_DOWNLOAD
        }
        val request =
            requestDownload(getString(R.string.app_name), getString(R.string.app_description))
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

    private fun DownloadManager.observerDownloadProgress() {
        val queryCursor = this.query(DownloadManager.Query().setFilterById(downloadID))
        queryCursor.handleStateButton()
    }

    @SuppressLint("Range")
    private fun Cursor?.handleStateButton() {
        with(this) {
            if (this != null && moveToFirst()) {
                val id = getInt(getColumnIndex(DownloadManager.COLUMN_ID))
                val status =
                    getInt(getColumnIndex(DownloadManager.COLUMN_STATUS)).also { status ->
                        Logger.d("Progress download with $id: is $status")
                    }
                when (status) {
                    DownloadManager.STATUS_RUNNING -> {
                        mBinding.layoutMain.btnLoading.changeStateOfButton(ButtonState.Loading)
                    }

                    DownloadManager.STATUS_SUCCESSFUL -> {
                        mBinding.layoutMain.btnLoading.changeStateOfButton(ButtonState.Completed)
                    }

                    else -> {
                        mBinding.layoutMain.btnLoading.changeStateOfButton(ButtonState.Completed)
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