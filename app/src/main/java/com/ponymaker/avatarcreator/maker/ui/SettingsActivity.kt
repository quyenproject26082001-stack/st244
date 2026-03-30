package com.ponymaker.avatarcreator.maker.ui

import android.view.LayoutInflater
import com.ponymaker.avatarcreator.maker.R
import com.ponymaker.avatarcreator.maker.core.base.BaseActivity
import com.ponymaker.avatarcreator.maker.core.extensions.gone
import com.ponymaker.avatarcreator.maker.core.extensions.handleBackLeftToRight
import com.ponymaker.avatarcreator.maker.core.extensions.policy
import com.ponymaker.avatarcreator.maker.core.extensions.select
import com.ponymaker.avatarcreator.maker.core.extensions.setImageActionBar
import com.ponymaker.avatarcreator.maker.core.extensions.setTextActionBar
import com.ponymaker.avatarcreator.maker.core.extensions.shareApp
import com.ponymaker.avatarcreator.maker.core.extensions.startIntentRightToLeft
import com.ponymaker.avatarcreator.maker.core.extensions.visible
import com.ponymaker.avatarcreator.maker.core.utils.key.IntentKey
import com.ponymaker.avatarcreator.maker.core.utils.state.RateState
import com.ponymaker.avatarcreator.maker.databinding.ActivitySettingsBinding
import com.ponymaker.avatarcreator.maker.ui.language.LanguageActivity
import com.ponymaker.avatarcreator.maker.core.extensions.tap
import com.ponymaker.avatarcreator.maker.core.helper.MusicHelper
import com.ponymaker.avatarcreator.maker.core.helper.SoundHelper
import com.ponymaker.avatarcreator.maker.core.helper.RateHelper
import kotlin.jvm.java

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {
    override fun setViewBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRate()
        initMusic()
        initEffect()
        binding.tvSetting.select()
    }

    private fun initMusic() {
        updateMusicUI(sharePreference.isMusicEnabled())
    }

    private fun updateMusicUI(isEnabled: Boolean) {
        binding.btnMusic.setImageResource(
            if (isEnabled) R.drawable.ic_sw_on else R.drawable.ic_sw_off
        )
    }

    private fun initEffect() {
        updateEffectUI(sharePreference.isEffectEnabled())
    }

    private fun updateEffectUI(isEnabled: Boolean) {
        binding.btnEffect.setImageResource(
            if (isEnabled) R.drawable.ic_sw_on else R.drawable.ic_sw_off
        )
    }

    private fun toggleEffect() {
        val isEnabled = !sharePreference.isEffectEnabled()
        sharePreference.setEffectEnabled(isEnabled)
        SoundHelper.isEffectEnabled = isEnabled
        updateEffectUI(isEnabled)
    }

    private fun toggleMusic() {
        val isEnabled = !sharePreference.isMusicEnabled()
        sharePreference.setMusicEnabled(isEnabled)
        updateMusicUI(isEnabled)
        if (isEnabled) {
            MusicHelper.play()
        } else {
            MusicHelper.pause()
        }
    }

    override fun viewListener() {
        binding.apply {
           btnActionBarLeft.tap { handleBackLeftToRight() }
            btnMusic.tap { toggleMusic() }
            btnEffect.tap { toggleEffect() }
            btnLang.tap { startIntentRightToLeft(LanguageActivity::class.java, IntentKey.INTENT_KEY) }
            btnShareApp.tap(1500) { shareApp() }
            btnRate.tap {
                RateHelper.showRateDialog(this@SettingsActivity, sharePreference){ state ->
                    if (state != RateState.CANCEL){
                        btnRate.gone()
                        showToast(R.string.have_rated)
                    }
                }
            }
            btnPolicy.tap(1500) { policy() }
        }
    }

    override fun initText() {
        //binding.actionBar.tvCenter.select()
    }

    override fun shouldPlayBackgroundMusic(): Boolean = true

    override fun initActionBar() {
//        binding.actionBar.apply {
//            setImageActionBar(btnActionBarLeft, R.drawable.ic_back)
//            setTextActionBar(tvCenter, getString(R.string.settings))
//        }
    }

    private fun initRate() {
        if (sharePreference.getIsRate(this)) {
            binding.btnRate.gone()
        } else {
            binding.btnRate.visible()
        }
    }
}