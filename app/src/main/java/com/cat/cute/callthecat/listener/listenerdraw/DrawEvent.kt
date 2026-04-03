package com.cat.cute.callthecat.listener.listenerdraw

import android.view.MotionEvent
import com.cat.cute.callthecat.core.custom.drawview.DrawView


interface DrawEvent {
    fun onActionDown(tattooView: DrawView?, event: MotionEvent?)
    fun onActionMove(tattooView: DrawView?, event: MotionEvent?)
    fun onActionUp(tattooView: DrawView?, event: MotionEvent?)
}