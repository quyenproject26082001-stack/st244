package com.ponymaker.avatarcreator.maker.listener.listenerdraw

import android.view.MotionEvent
import com.ponymaker.avatarcreator.maker.core.custom.drawview.DrawView


interface DrawEvent {
    fun onActionDown(tattooView: DrawView?, event: MotionEvent?)
    fun onActionMove(tattooView: DrawView?, event: MotionEvent?)
    fun onActionUp(tattooView: DrawView?, event: MotionEvent?)
}