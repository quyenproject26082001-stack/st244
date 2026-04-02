package com.temppp.ui.studio

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.temppp.core.base.BaseActivity
import com.temppp.databinding.ActivityStudioBinding
import com.temppp.ui.splash.SplashActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class StudioActivity : BaseActivity<ActivityStudioBinding>() {

    override fun setViewBinding(): ActivityStudioBinding {
        return ActivityStudioBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        lifecycleScope.launch {
            delay(1000L)
            startActivity(Intent(this@StudioActivity, SplashActivity::class.java))
            finish()
        }
    }

    override fun viewListener() {}
    override fun initActionBar() {}

    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    override fun onBackPressed() {}

    override fun shouldPlayBackgroundMusic(): Boolean = false
}
