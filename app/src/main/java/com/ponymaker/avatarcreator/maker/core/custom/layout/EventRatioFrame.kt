package com.ponymaker.avatarcreator.maker.core.custom.layout

import android.widget.ImageView
import com.ponymaker.avatarcreator.maker.core.custom.imageview.StrokeImageView

interface EventRatioFrame {
    fun onImageClick(image: StrokeImageView, btnEdit: ImageView)
}