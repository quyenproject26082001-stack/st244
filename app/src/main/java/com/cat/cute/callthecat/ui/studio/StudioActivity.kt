package com.cat.cute.callthecat.ui.studio

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import com.airbnb.lottie.LottieAnimationView
import com.cat.cute.callthecat.core.base.BaseActivity
import com.cat.cute.callthecat.databinding.ActivityStudioBinding
import com.cat.cute.callthecat.ui.splash.SplashActivity

@SuppressLint("CustomSplashScreen")
class StudioActivity : BaseActivity<ActivityStudioBinding>() {

    override fun setViewBinding(): ActivityStudioBinding {
        return ActivityStudioBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        binding.lottieTitle.addAnimatorListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                startActivity(Intent(this@StudioActivity, SplashActivity::class.java))
                finish()
            }
            override fun onAnimationStart(animation: android.animation.Animator) {}
            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
        })
    }

    override fun viewListener() {}
    override fun initActionBar() {}

    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    override fun onBackPressed() {}

    override fun shouldPlayBackgroundMusic(): Boolean = false
}
