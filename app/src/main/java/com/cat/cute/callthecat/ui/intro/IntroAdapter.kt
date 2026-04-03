package com.cat.cute.callthecat.ui.intro

import android.content.Context
import com.cat.cute.callthecat.core.base.BaseAdapter
import com.cat.cute.callthecat.core.extensions.loadImage
import com.cat.cute.callthecat.core.extensions.select
import com.cat.cute.callthecat.core.extensions.strings
import com.cat.cute.callthecat.data.model.IntroModel
import com.cat.cute.callthecat.databinding.ItemIntroBinding

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