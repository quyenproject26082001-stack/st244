package com.temppp.ui.call

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.temppp.core.base.BaseActivity
import com.temppp.core.extensions.handleBackLeftToRight
import com.temppp.core.extensions.tap
import com.temppp.core.helper.MusicHelper
import com.temppp.core.helper.SoundHelper
import com.temppp.data.local.DatabaseProvider
import com.temppp.databinding.ActivityCallBinding
import com.temppp.ui.home.formatCoins
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class CallActivity : BaseActivity<ActivityCallBinding>() {

    private var timerJob: Job? = null
    private var elapsedSeconds = 0
    private var coinsEarned = 0L
    private var savedVideoPosition = 0

    companion object {
        private const val EXTRA_CAT_ID = "extra_cat_id"
        fun start(context: Context, catId: Int) {
            context.startActivity(Intent(context, CallActivity::class.java).apply {
                putExtra(EXTRA_CAT_ID, catId)
            })
        }
    }

    override fun setViewBinding(): ActivityCallBinding =
        ActivityCallBinding.inflate(LayoutInflater.from(this))

    override fun initView() {
        val catId = intent.getIntExtra(EXTRA_CAT_ID, 1)
        playCatVideo(catId)
        startCallTimer()
        startPassiveCoinsWhileOnCall()
    }

    override fun viewListener() {
        binding.btnHangUp.tap(0) {
            saveEarnedCoins()
            binding.imgCallCat.stopPlayback()
            handleBackLeftToRight()
        }
    }

    override fun initActionBar() {}

    override fun shouldPlayBackgroundMusic(): Boolean = false

    override fun onResume() {
        super.onResume()
        MusicHelper.pause()
        SoundHelper.isEffectEnabled = false
        if (savedVideoPosition > 0) {
            binding.imgCallCat.seekTo(savedVideoPosition)
            binding.imgCallCat.start()
        }
    }

    override fun onPause() {
        super.onPause()
        savedVideoPosition = binding.imgCallCat.currentPosition
        binding.imgCallCat.pause()
        SoundHelper.isEffectEnabled = sharePreference.isEffectEnabled()
    }

    private fun playCatVideo(catId: Int) {
        val assetPath = "video/output_$catId.mp4"
        try {
            val tmpFile = File(cacheDir, "call_video_$catId.mp4")
            if (!tmpFile.exists()) {
                assets.open(assetPath).use { input ->
                    tmpFile.outputStream().use { output -> input.copyTo(output) }
                }
            }
            binding.imgCallCat.setVideoURI(Uri.fromFile(tmpFile))
            binding.imgCallCat.setOnPreparedListener { mp -> mp.isLooping = true }
            binding.imgCallCat.start()
        } catch (_: Exception) {
            // Video not found for this cat — leave VideoView empty
        }
    }

    private fun startCallTimer() {
        timerJob = lifecycleScope.launch {
            while (true) {
                delay(1_000)
                elapsedSeconds++
                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60
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
        try { binding.imgCallCat.stopPlayback() } catch (_: Exception) {}
    }
}
