package com.temppp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.temppp.R
import com.temppp.core.base.BaseFragment
import com.temppp.core.extensions.gone
import com.temppp.core.extensions.policy
import com.temppp.core.extensions.select
import com.temppp.core.extensions.shareApp
import com.temppp.core.extensions.tap
import com.temppp.core.extensions.visible
import com.temppp.core.helper.MusicHelper
import com.temppp.core.helper.RateHelper
import com.temppp.core.helper.SoundHelper
import com.temppp.core.utils.state.RateState
import com.temppp.databinding.ActivitySettingsBinding
import com.temppp.ui.home.HomeViewModel

class SettingsFragment : BaseFragment<ActivitySettingsBinding>() {

    private val viewModel: HomeViewModel by activityViewModels()

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
            btnClose.tap { parentFragmentManager.popBackStack() }
            btnMusic.tap { toggleMusic() }
            btnSound.tap { toggleEffect() }
            btnLang.tap {
                (requireActivity() as com.temppp.ui.MainActivity).navigateTo(com.temppp.ui.MainActivity.TAG_LANGUAGE)
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
    }

    private fun initRate() {
        if (sharePreference.getIsRate(requireContext())) {
            binding.btnRate.gone()
        } else {
            binding.btnRate.visible()
        }
    }
}
