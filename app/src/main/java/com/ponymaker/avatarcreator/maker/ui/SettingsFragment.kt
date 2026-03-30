package com.ponymaker.avatarcreator.maker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.ponymaker.avatarcreator.maker.R
import com.ponymaker.avatarcreator.maker.core.base.BaseFragment
import com.ponymaker.avatarcreator.maker.core.extensions.gone
import com.ponymaker.avatarcreator.maker.core.extensions.policy
import com.ponymaker.avatarcreator.maker.core.extensions.select
import com.ponymaker.avatarcreator.maker.core.extensions.shareApp
import com.ponymaker.avatarcreator.maker.core.extensions.startIntentRightToLeft
import com.ponymaker.avatarcreator.maker.core.extensions.tap
import com.ponymaker.avatarcreator.maker.core.extensions.visible
import com.ponymaker.avatarcreator.maker.core.helper.MusicHelper
import com.ponymaker.avatarcreator.maker.core.helper.RateHelper
import com.ponymaker.avatarcreator.maker.core.helper.SoundHelper
import com.ponymaker.avatarcreator.maker.core.utils.key.IntentKey
import com.ponymaker.avatarcreator.maker.core.utils.state.RateState
import com.ponymaker.avatarcreator.maker.databinding.ActivitySettingsBinding
import com.ponymaker.avatarcreator.maker.ui.language.LanguageActivity

class SettingsFragment : BaseFragment<ActivitySettingsBinding>() {

    override fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ActivitySettingsBinding.inflate(inflater, container, false)

    override fun initView() {
        initRate()
        updateMusicUI(sharePreference.isMusicEnabled())
        updateEffectUI(sharePreference.isEffectEnabled())
        binding.tvSetting.select()
    }

    override fun viewListener() {
        binding.apply {
            btnActionBarLeft.tap { parentFragmentManager.popBackStack() }
            btnMusic.tap { toggleMusic() }
            btnEffect.tap { toggleEffect() }
            btnLang.tap {
                requireActivity().startIntentRightToLeft(LanguageActivity::class.java, IntentKey.INTENT_KEY)
            }
            btnShareApp.tap(1500) { requireActivity().shareApp() }
            btnRate.tap {
                RateHelper.showRateDialog(requireActivity(), sharePreference) { state ->
                    if (state != RateState.CANCEL) {
                        btnRate.gone()
                        showToast(R.string.have_rated)
                    }
                }
            }
            btnPolicy.tap(1500) { requireActivity().policy() }
        }
    }

    private fun updateMusicUI(isEnabled: Boolean) {
        binding.btnMusic.setImageResource(
            if (isEnabled) R.drawable.ic_sw_on else R.drawable.ic_sw_off
        )
    }

    private fun updateEffectUI(isEnabled: Boolean) {
        binding.btnEffect.setImageResource(
            if (isEnabled) R.drawable.ic_sw_on else R.drawable.ic_sw_off
        )
    }

    private fun toggleMusic() {
        val isEnabled = !sharePreference.isMusicEnabled()
        sharePreference.setMusicEnabled(isEnabled)
        updateMusicUI(isEnabled)
        if (isEnabled) MusicHelper.play() else MusicHelper.pause()
    }

    private fun toggleEffect() {
        val isEnabled = !sharePreference.isEffectEnabled()
        sharePreference.setEffectEnabled(isEnabled)
        SoundHelper.isEffectEnabled = isEnabled
        updateEffectUI(isEnabled)
    }

    private fun initRate() {
        if (sharePreference.getIsRate(requireContext())) {
            binding.btnRate.gone()
        } else {
            binding.btnRate.visible()
        }
    }
}
