package com.ponymaker.avatarcreator.maker.core.helper

import android.content.Context
import android.media.SoundPool

object SoundHelper {
    private val soundPool = SoundPool.Builder().setMaxStreams(5).build()
    private val soundMap = mutableMapOf<Int, Int>()
    var isEffectEnabled: Boolean = true

    fun isSoundNotNull(resId: Int) : Boolean {
        return soundMap[resId] != null
    }

    fun loadSound(context: Context, resId: Int) {
        if (soundMap[resId] == null) {
            soundMap[resId] = soundPool.load(context, resId, 1)
        }
    }

    fun playSound(resId: Int) {
        if (!isEffectEnabled) return
        soundMap[resId]?.let { id ->
            soundPool.play(id, 1f, 1f, 0, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}