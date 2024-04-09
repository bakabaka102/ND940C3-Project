package com.udacity

import com.udacity.base.BaseActivity
import com.udacity.databinding.ActivityDetailBinding

class DetailActivity : BaseActivity<ActivityDetailBinding>() {

    override fun instanceViewBinding(): ActivityDetailBinding {
        return ActivityDetailBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        setSupportActionBar(mBinding.toolbar)

    }

    override fun initActions() {

    }

}
