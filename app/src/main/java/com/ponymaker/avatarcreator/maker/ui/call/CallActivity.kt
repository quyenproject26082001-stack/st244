package com.ponymaker.avatarcreator.maker.ui.call

import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.ponymaker.avatarcreator.maker.R
import com.ponymaker.avatarcreator.maker.core.base.BaseActivity
import com.ponymaker.avatarcreator.maker.core.extensions.handleBackLeftToRight
import com.ponymaker.avatarcreator.maker.core.extensions.tap
import com.ponymaker.avatarcreator.maker.data.local.DatabaseProvider
import com.ponymaker.avatarcreator.maker.databinding.ActivityCallBinding
import com.ponymaker.avatarcreator.maker.ui.home.formatCoins
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CallActivity : BaseActivity<ActivityCallBinding>() {

    private var timerJob: Job? = null
    private var elapsedSeconds = 0
    private var coinsEarned = 0L

    override fun setViewBinding(): ActivityCallBinding =
        ActivityCallBinding.inflate(LayoutInflater.from(this))

    override fun initView() {
        loadSelectedCat()
        startCallTimer()
        startPassiveCoinsWhileOnCall()
    }

    override fun viewListener() {
        binding.btnHangUp.tap(0) {
            saveEarnedCoins()
            handleBackLeftToRight()
        }
    }

    override fun initActionBar() {}

    override fun shouldPlayBackgroundMusic(): Boolean = false

    private fun loadSelectedCat() {
        lifecycleScope.launch {
            val db = DatabaseProvider.getDatabase(this@CallActivity)
            val cat = db.catContactDao().getSelectedCat()
            if (cat != null) {
                val resId = resources.getIdentifier(cat.imageRes, "drawable", packageName)
                binding.imgCallCat.setImageResource(if (resId != 0) resId else R.drawable.ic_loading)
            }
        }
    }

    private fun startCallTimer() {
        timerJob = lifecycleScope.launch {
            while (true) {
                delay(1_000)
                elapsedSeconds++
                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60
                binding.tvCallTimer.text = "%02d:%02d".format(minutes, seconds)
            }
        }
    }

    private fun startPassiveCoinsWhileOnCall() {
        lifecycleScope.launch {
            val db = DatabaseProvider.getDatabase(this@CallActivity)
            while (true) {
                delay(1_000)
                val state = db.gameStateDao().getGameStateOnce() ?: continue
                if (state.coinsPerSecond > 0) {
                    coinsEarned += state.coinsPerSecond
                    binding.tvCallCoins.text = "+${formatCoins(coinsEarned)} coins"
                }
            }
        }
    }

    private fun saveEarnedCoins() {
        if (coinsEarned > 0) {
            lifecycleScope.launch {
                val db = DatabaseProvider.getDatabase(this@CallActivity)
                val state = db.gameStateDao().getGameStateOnce() ?: return@launch
                db.gameStateDao().updateCoins(state.coins + coinsEarned)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
    }
}
