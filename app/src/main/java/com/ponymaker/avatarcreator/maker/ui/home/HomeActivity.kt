package com.ponymaker.avatarcreator.maker.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.animation.ScaleAnimation
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ponymaker.avatarcreator.maker.R
import com.ponymaker.avatarcreator.maker.core.base.BaseActivity
import com.ponymaker.avatarcreator.maker.core.extensions.startIntentRightToLeft
import com.ponymaker.avatarcreator.maker.core.extensions.tap
import com.ponymaker.avatarcreator.maker.core.helper.LanguageHelper
import com.ponymaker.avatarcreator.maker.core.utils.state.RateState
import com.ponymaker.avatarcreator.maker.core.extensions.rateApp
import com.ponymaker.avatarcreator.maker.databinding.ActivityHomeBinding
import com.ponymaker.avatarcreator.maker.ui.SettingsActivity
import com.ponymaker.avatarcreator.maker.ui.call.CallActivity
import com.ponymaker.avatarcreator.maker.ui.contacts.ContactsActivity
import com.ponymaker.avatarcreator.maker.ui.shop.ShopActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    private lateinit var viewModel: HomeViewModel

    override fun setViewBinding(): ActivityHomeBinding =
        ActivityHomeBinding.inflate(LayoutInflater.from(this))

    override fun initView() {
        viewModel = ViewModelProvider.AndroidViewModelFactory
            .getInstance(application)
            .create(HomeViewModel::class.java)
        sharePreference.setCountBack(sharePreference.getCountBack() + 1)
    }

    override fun viewListener() {
        binding.apply {
            btnPhone.tap(0) {
                viewModel.onPhoneClick()
                animatePhoneButton()
            }
            btnClickBoost.tap(500) {
                viewModel.activateClickBoost()
                showToast("x2 Click boost activated!")
            }
            btnPassiveBoost.tap(500) {
                viewModel.activatePassiveBoost()
                showToast("x2 Passive boost activated!")
            }
            btnNavShop.tap(300) {
                startIntentRightToLeft(ShopActivity::class.java)
            }
            btnNavContacts.tap(300) {
                startIntentRightToLeft(ContactsActivity::class.java)
            }
            btnNavSettings.tap(300) {
                startIntentRightToLeft(SettingsActivity::class.java)
            }
        }
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            launch {
                viewModel.coins.collectLatest { coins ->
                    updateCoinDisplay(coins)
                }
            }
            launch {
                viewModel.coinsPerClick.collectLatest { cpc ->
                    binding.tvCoinsPerClick.text = "${formatCoins(cpc)} /click"
                }
            }
            launch {
                viewModel.coinsPerSecond.collectLatest { cps ->
                    binding.tvCoinsPerSecond.text = "${formatCoins(cps)} /second"
                }
            }
            launch {
                viewModel.clickBoostActive.collectLatest { active ->
                    if (active) {
                        binding.tvCoinsPerClick.setTextColor(getColor(R.color.white))
                        binding.tvCoinsPerClick.setBackgroundResource(R.drawable.bg_boost_click)
                    } else {
                        binding.tvCoinsPerClick.setTextColor(0xFF2ECC71.toInt())
                        binding.tvCoinsPerClick.setBackgroundResource(R.drawable.bg_stat_pill)
                    }
                }
            }
            launch {
                viewModel.boostProgress.collectLatest { progress ->
                    val isVisible = progress > 0f
                    binding.layoutClickBoostActive.visibility =
                        if (isVisible) android.view.View.VISIBLE else android.view.View.GONE
                    binding.progressBoost.progress = (progress * 100).toInt()
                }
            }
            launch {
                viewModel.selectedCat.collectLatest { cat ->
                    if (cat != null) {
                        loadCatImage(cat.imageRes)
                    }
                }
            }
        }
    }

    override fun initActionBar() {}

    override fun shouldPlayBackgroundMusic(): Boolean = true

    override fun onRestart() {
        super.onRestart()
        LanguageHelper.setLocale(this)
        viewModel.refreshStats()
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveCoins()
    }

    private fun updateCoinDisplay(coins: Long) {
        val milestone = nextMilestone(coins)
        binding.tvCoinCount.text = "${formatCoins(coins)} / ${formatCoins(milestone)}"
    }

    private fun nextMilestone(coins: Long): Long {
        var m = 1_000L
        while (m <= coins) m *= 10
        return m
    }

    private fun animatePhoneButton() {
        val scaleAnim = ScaleAnimation(
            1f, 0.85f, 1f, 0.85f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        )
        scaleAnim.duration = 80
        scaleAnim.repeatCount = 1
        scaleAnim.repeatMode = android.view.animation.Animation.REVERSE
        binding.btnPhone.startAnimation(scaleAnim)
    }

    private fun loadCatImage(imageRes: String) {
        val resId = resources.getIdentifier(imageRes, "drawable", packageName)
        if (resId != 0) {
            binding.imgCat.setImageResource(resId)
        } else {
            binding.imgCat.setImageResource(R.drawable.ic_loading)
        }
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        if (!sharePreference.getIsRate(this) && sharePreference.getCountBack() % 2 == 0) {
            rateApp(sharePreference) { state ->
                if (state != RateState.CANCEL) {
                    showToast(R.string.have_rated)
                }
                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        delay(1000)
                        exitProcess(0)
                    }
                }
            }
        } else {
            exitProcess(0)
        }
    }

    override fun initAds() {
        // Ad initialization here when ready
    }
}

fun formatCoins(value: Long): String = when {
    value >= 1_000_000_000_000L -> "${value / 1_000_000_000_000L}T"
    value >= 1_000_000_000L     -> "${value / 1_000_000_000L}B"
    value >= 1_000_000L         -> "${value / 1_000_000L}M"
    value >= 1_000L             -> "${value / 1_000L}K"
    else                        -> value.toString()
}
