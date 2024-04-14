package com.udacity.download

sealed class DownloadStatus() {

    object Successful : DownloadStatus()
    object Failed : DownloadStatus()
    object Unknown : DownloadStatus()

}