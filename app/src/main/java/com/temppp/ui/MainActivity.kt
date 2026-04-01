package com.temppp.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.temppp.R
import com.temppp.core.base.BaseActivity
import com.temppp.core.extensions.rateApp
import com.temppp.core.utils.state.RateState
import com.temppp.databinding.ActivityMainBinding
import com.temppp.ui.contacts.ContactsFragment
import com.temppp.ui.home.HomeFragment
import com.temppp.ui.shop.ShopFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun setViewBinding() = ActivityMainBinding.inflate(LayoutInflater.from(this))

    override fun initView() {
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment(), TAG_HOME)
                .commit()
        }
    }

    override fun viewListener() {}

    override fun initActionBar() {}

    override fun shouldPlayBackgroundMusic() = true

    fun navigateTo(tag: String) {
        val fragment = when (tag) {
            TAG_SHOP -> ShopFragment()
            TAG_CONTACTS -> ContactsFragment()
            TAG_SETTINGS -> SettingsFragment()
            else -> return
        }
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right
            )
            .replace(R.id.fragmentContainer, fragment, tag)
            .addToBackStack(null)
            .commit()
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
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
    }

    companion object {
        const val TAG_HOME = "home"
        const val TAG_SHOP = "shop"
        const val TAG_CONTACTS = "contacts"
        const val TAG_SETTINGS = "settings"
    }
}
