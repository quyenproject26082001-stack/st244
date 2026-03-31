package com.ponymaker.avatarcreator.maker.ui.shop

import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ponymaker.avatarcreator.maker.R
import com.ponymaker.avatarcreator.maker.data.local.entity.ShopUpgrade
import com.ponymaker.avatarcreator.maker.databinding.ItemShopUpgradeBinding
import com.ponymaker.avatarcreator.maker.ui.home.formatCoins

class ShopAdapter(
    private val onBuyClick: (ShopUpgrade) -> Unit
) : ListAdapter<ShopUpgrade, ShopAdapter.ViewHolder>(DIFF) {

    var currentCoins: Long = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class ViewHolder(private val binding: ItemShopUpgradeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(upgrade: ShopUpgrade) {
            val levelDisplay = if (upgrade.level == 0) "lv.1" else "lv.${upgrade.level + 1}"
            binding.tvUpgradeName.text = "${upgrade.name} $levelDisplay"
            val context = binding.root.context
            val resId = context.resources.getIdentifier(
                upgrade.name.lowercase().replace(" ", "_"), "drawable", context.packageName
            )
            if (resId != 0) binding.imgUpgradeIcon.setImageResource(resId)
            binding.tvUpgradeDesc.text = descWithCoinIcon(upgrade.description)
            binding.btnBuy.text = priceWithCoinIcon(formatCoins(upgrade.price))
            binding.btnBuy.setBackgroundResource(
                if (currentCoins >= upgrade.price) R.drawable.bg_price_btn
                else R.drawable.bg_price_btn_uslt
            )
            binding.btnBuy.setOnClickListener { onBuyClick(upgrade) }
        }

        private fun priceWithCoinIcon(price: String): SpannableString {
            val text = "$price "
            val span = SpannableString(text)
            val context = binding.root.context
            val size = (16 * context.resources.displayMetrics.density).toInt()
            val drawable: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_coin_progress)!!
            drawable.setBounds(0, 0, size, size)
            span.setSpan(ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM), text.length - 1, text.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            return span
        }

        private fun descWithCoinIcon(desc: String): SpannableString {
            val placeholder = " "
            val text = desc.replace("coins", placeholder)
            val span = SpannableString(text)
            val idx = text.indexOf(placeholder)
            if (idx >= 0) {
                val context = binding.root.context
                val size = (16 * context.resources.displayMetrics.density).toInt()
                val drawable: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_coin_progress)!!
                drawable.setBounds(0, 0, size, size)
                span.setSpan(ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM), idx, idx + 1, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            return span
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShopUpgradeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ShopUpgrade>() {
            override fun areItemsTheSame(a: ShopUpgrade, b: ShopUpgrade) = a.id == b.id
            override fun areContentsTheSame(a: ShopUpgrade, b: ShopUpgrade) = a == b
        }
    }
}
