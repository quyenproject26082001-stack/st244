package com.ponymaker.avatarcreator.maker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
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
import com.ponymaker.avatarcreator.maker.core.utils.state.RateState
import com.ponymaker.avatarcreator.maker.databinding.ActivitySettingsBinding
import com.ponymaker.avatarcreator.maker.ui.home.HomeViewModel
import com.ponymaker.avatarcreator.maker.ui.home.initTopBar
import com.ponymaker.avatarcreator.maker.ui.home.observeTopBar
import com.ponymaker.avatarcreator.maker.ui.language.LanguageSettingActivity

class SettingsFragment : BaseFragment<ActivitySettingsBinding>() {

    private val viewModel: HomeViewModel by activityViewModels()

    override fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ActivitySettingsBinding.inflate(inflater, container, false)

    override fun initView() {
        initRate()
        updateMusicUI(sharePreference.isMusicEnabled())
        updateEffectUI(sharePreference.isEffectEnabled())
        binding.tvSetting.select()
        initTopBar(binding.topBar)
    }

    override fun viewListener() {
        binding.apply {
            btnCloseSetting.tap { parentFragmentManager.popBackStack() }
            btnMusic.tap { toggleMusic() }
            btnSound.tap { toggleEffect() }
            btnLang.tap {
                requireActivity().startIntentRightToLeft(LanguageSettingActivity::class.java)
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
        binding.btnMusic.setBackgroundResource(
            if (isEnabled) R.drawable.ic_sw_on else R.drawable.ic_sw_off
        )
    }

    private fun updateEffectUI(isEnabled: Boolean) {
        binding.btnSound.setBackgroundResource(
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

    override fun dataObservable() {
        observeTopBar(binding.topBar, viewModel)
    }

    private fun initRate() {
        if (sharePreference.getIsRate(requireContext())) {
            binding.btnRate.gone()
        } else {
            binding.btnRate.visible()
        }
    }
}
