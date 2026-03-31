package com.ponymaker.avatarcreator.maker.ui.language

import android.annotation.SuppressLint
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ponymaker.avatarcreator.maker.R
import com.ponymaker.avatarcreator.maker.core.base.BaseActivity
import com.ponymaker.avatarcreator.maker.core.extensions.handleBackLeftToRight
import com.ponymaker.avatarcreator.maker.core.extensions.select
import com.ponymaker.avatarcreator.maker.core.extensions.startIntentWithClearTop
import com.ponymaker.avatarcreator.maker.core.extensions.tap
import com.ponymaker.avatarcreator.maker.core.extensions.visible
import com.ponymaker.avatarcreator.maker.databinding.ActivityLanguageSettingBinding
import com.ponymaker.avatarcreator.maker.ui.MainActivity
import com.ponymaker.avatarcreator.maker.ui.home.HomeViewModel
import com.ponymaker.avatarcreator.maker.ui.home.initTopBar
import com.ponymaker.avatarcreator.maker.ui.home.observeTopBar
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
