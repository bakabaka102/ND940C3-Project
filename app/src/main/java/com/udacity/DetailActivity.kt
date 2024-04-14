package com.udacity

import com.udacity.base.BaseActivity
import com.udacity.databinding.ActivityDetailBinding
import com.udacity.utils.Constants

class DetailActivity : BaseActivity<ActivityDetailBinding>() {

    override fun instanceViewBinding(): ActivityDetailBinding {
        return ActivityDetailBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        setSupportActionBar(mBinding.toolbar)
         val fileName by lazy {
            intent?.extras?.getString(Constants.FILE_NAME, "unknownText") ?: "unknownText"
        }
        val downloadStatus by lazy {
            intent?.extras?.getString(Constants.DOWNLOAD_STATUS, "unknownText") ?: "unknownText"
        }

    }

    override fun initActions() {

    }

}
