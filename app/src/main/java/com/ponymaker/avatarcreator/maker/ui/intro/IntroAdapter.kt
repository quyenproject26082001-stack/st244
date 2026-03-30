package com.ponymaker.avatarcreator.maker.ui.intro

import android.content.Context
import com.ponymaker.avatarcreator.maker.core.base.BaseAdapter
import com.ponymaker.avatarcreator.maker.core.extensions.loadImage
import com.ponymaker.avatarcreator.maker.core.extensions.select
import com.ponymaker.avatarcreator.maker.core.extensions.strings
import com.ponymaker.avatarcreator.maker.data.model.IntroModel
import com.ponymaker.avatarcreator.maker.databinding.ItemIntroBinding

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