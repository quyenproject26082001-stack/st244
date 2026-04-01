package com.temppp.ui.language

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.temppp.R
import com.temppp.core.base.BaseFragment
import com.temppp.core.extensions.tap
import com.temppp.core.extensions.visible
import com.temppp.databinding.ActivityLanguageSettingBinding
import kotlinx.coroutines.launch

class LanguageFragment : BaseFragment<ActivityLanguageSettingBinding>() {

    private val viewModel: LanguageViewModel by viewModels()
    private val languageAdapter by lazy { LanguageAdapter(requireContext()) }

    override fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ActivityLanguageSettingBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.rcv.adapter = languageAdapter
        binding.rcv.itemAnimator = null
        val currentLang = sharePreference.getPreLanguage()
        viewModel.setFirstLanguage(false)
        viewModel.loadLanguages(currentLang)
    }

    override fun viewListener() {
        binding.btnBackLangSetting.tap { parentFragmentManager.popBackStack() }
        binding.btnDoneLangSetting.tap { handleDone() }
        languageAdapter.onItemClick = { code -> viewModel.selectLanguage(code) }
    }

    override fun dataObservable() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.languageList.collect { list ->
                        languageAdapter.submitList(list)
                    }
                }
                launch {
                    viewModel.codeLang.collect { code ->
                        if (code.isNotEmpty()) binding.btnDoneLangSetting.visible()
                    }
                }
            }
        }
    }

    private fun handleDone() {
        val code = viewModel.codeLang.value
        if (code.isEmpty()) {
            showToast(R.string.not_select_lang)
            return
        }
        sharePreference.setPreLanguage(code)
        parentFragmentManager.popBackStack()
    }
}
