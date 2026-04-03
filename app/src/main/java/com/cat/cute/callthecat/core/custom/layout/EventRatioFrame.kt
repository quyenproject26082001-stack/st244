package com.cat.cute.callthecat.core.custom.layout

import android.widget.ImageView
import com.cat.cute.callthecat.core.custom.imageview.StrokeImageView

interface EventRatioFrame {
    fun onImageClick(image: StrokeImageView, btnEdit: ImageView)
}