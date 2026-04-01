package com.temppp.ui.intro

import android.content.Context
import com.temppp.core.base.BaseAdapter
import com.temppp.core.extensions.loadImage
import com.temppp.core.extensions.select
import com.temppp.core.extensions.strings
import com.temppp.data.model.IntroModel
import com.temppp.databinding.ItemIntroBinding

class IntroAdapter(val context: Context) : BaseAdapter<IntroModel, ItemIntroBinding>(
    ItemIntroBinding::inflate
) {
    override fun onBind(binding: ItemIntroBinding, item: IntroModel, position: Int) {
        binding.apply {
            loadImage(root, item.image, imvImage, false)
            tvContent.text = context.strings(item.content)
            tvContent.select()
        }
    }
}