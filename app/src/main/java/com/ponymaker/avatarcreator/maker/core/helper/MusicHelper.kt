package com.ponymaker.avatarcreator.maker.core.helper

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.ponymaker.avatarcreator.maker.R

object MusicHelper {
    private var mediaPlayer: MediaPlayer? = null
    private var isPrepared = false
    private val handler = Handler(Looper.getMainLooper())
    private val pauseRunnable = Runnable { doPause() }

    fun init(context: Context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.sound)
            mediaPlayer?.isLooping = true
            isPrepared = true
        }
    }

    fun play() {
        handler.removeCallbacks(pauseRunnable)
        if (isPrepared && mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    fun pause() {
        handler.removeCallbacks(pauseRunnable)
        handler.postDelayed(pauseRunnable, 300)
    }

    private fun doPause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    fun stop() {
        handler.removeCallbacks(pauseRunnable)
        mediaPlayer?.stop()
        isPrepared = false
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        isPrepared = false
    }
}
