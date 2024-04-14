package com.udacity.download

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.net.Uri
import com.udacity.utils.Constants

@SuppressLint("Range")
fun DownloadManager.statusQuery(id: Long): String {
    val cursor = this.query(DownloadManager.Query().setFilterById(id))
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

fun requestDownload(title: String, description: String): DownloadManager.Request? {
    return DownloadManager.Request(Uri.parse(Constants.URL))
        .setTitle(title)
        .setDescription(description)
        .setRequiresCharging(false)
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)
}
