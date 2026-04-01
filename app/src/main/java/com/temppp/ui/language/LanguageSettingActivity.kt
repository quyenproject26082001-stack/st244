package com.temppp.ui.language

import android.annotation.SuppressLint
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.temppp.R
import com.temppp.core.base.BaseActivity
import com.temppp.core.extensions.handleBackLeftToRight
import com.temppp.core.extensions.select
import com.temppp.core.extensions.startIntentWithClearTop
import com.temppp.core.extensions.tap
import com.temppp.core.extensions.visible
import com.temppp.databinding.ActivityLanguageSettingBinding
import com.temppp.ui.MainActivity
import com.temppp.ui.home.HomeViewModel
import com.temppp.ui.home.initTopBar
import com.temppp.ui.home.observeTopBar
import kotlinx.coroutines.launch

class LanguageSettingActivity : BaseActivity<ActivityLanguageSettingBinding>() {

    private val viewModel: LanguageViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    private val languageAdapter by lazy { LanguageAdapter(this) }

    override fun setViewBinding(): ActivityLanguageSettingBinding {
        return ActivityLanguageSettingBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        binding.rcv.adapter = languageAdapter
        binding.rcv.itemAnimator = null
        val currentLang = sharePreference.getPreLanguage()
        viewModel.setFirstLanguage(false)
        viewModel.loadLanguages(currentLang)
        binding.tvLang.select()
        initTopBar(binding.topBar)
    }

    override fun dataObservable() {
        observeTopBar(binding.topBar, homeViewModel)
        lifecycleScope.launch {
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

    override fun viewListener() {
        binding.btnBackLangSetting.tap { handleBackLeftToRight() }
        binding.btnDoneLangSetting.tap { handleDone() }
        binding.btnDoneLangSetting.tap { handleDone() }
        languageAdapter.onItemClick = { code -> viewModel.selectLanguage(code) }
    }

    override fun initText() {}
    override fun initActionBar() {}

    private fun handleDone() {
        val code = viewModel.codeLang.value
        if (code.isEmpty()) {
            showToast(R.string.not_select_lang)
            return
        }
        sharePreference.setPreLanguage(code)
        startIntentWithClearTop(MainActivity::class.java)
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        handleBackLeftToRight()
    }

    override fun shouldPlayBackgroundMusic(): Boolean = false
}
