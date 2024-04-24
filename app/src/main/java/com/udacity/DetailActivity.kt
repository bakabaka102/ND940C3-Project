package com.udacity

import android.annotation.SuppressLint
import com.udacity.base.BaseActivity
import com.udacity.databinding.ActivityDetailBinding
import com.udacity.utils.Constants
import com.udacity.utils.Logger

class DetailActivity : BaseActivity<ActivityDetailBinding>() {

    override fun instanceViewBinding(): ActivityDetailBinding {
        return ActivityDetailBinding.inflate(layoutInflater)
    }

    @SuppressLint("NewApi")
    override fun initViews() {
        setSupportActionBar(mBinding.toolbar)
        val fileName by lazy {
            intent?.extras?.getString(Constants.FILE_NAME, "unknownText") ?: "unknownText"
        }
        val downloadStatus by lazy {
            intent?.extras?.getString(Constants.DOWNLOAD_STATUS, "unknownText") ?: "unknownText"
        }
        Logger.d("filename: $fileName  --- downloadStatus: $downloadStatus")
        mBinding.layoutDetailContent.fileNameText.text = getString(R.string.file_name, fileName)
        mBinding.layoutDetailContent.downloadStatusTextLabel.text =
            getString(R.string.download_status, downloadStatus)

    }

    override fun initActions() {
        mBinding.layoutDetailContent.btnOK.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

}
