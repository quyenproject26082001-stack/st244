package com.temppp.ui.shop

import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.temppp.R
import com.temppp.data.local.entity.ShopUpgrade
import com.temppp.databinding.ItemShopUpgradeBinding
import com.temppp.ui.home.formatCoins

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
            val desc = if (upgrade.level == 0) upgrade.description.replace(Regex("[0-9][0-9KMB.,]*"), "???") else upgrade.description
            binding.tvUpgradeDesc.text = descWithCoinIcon(desc)
            binding.tvBuyPrice.text = formatCoins(upgrade.price)
            binding.btnBuy.setBackgroundResource(
                if (currentCoins >= upgrade.price) R.drawable.bg_price_btn
                else R.drawable.bg_price_btn_uslt
            )
            binding.btnBuy.setOnClickListener { onBuyClick(upgrade) }
        }

        // "coins" → thin-space + icon placeholder + thin-space (~2dp margin each side)
        private fun descWithCoinIcon(desc: String): SpannableString {
            val ICON = '\uFFFD'
            val THIN = '\u2009' // thin space ≈ 2dp at 13sp
            val text = desc.replace("coins", "$THIN$ICON$THIN")
            val span = SpannableString(text)
            val idx = text.indexOf(ICON)
            if (idx >= 0) {
                val size = (16 * binding.root.context.resources.displayMetrics.density).toInt()
                val drawable: Drawable = ContextCompat.getDrawable(binding.root.context, R.drawable.ic_coin_progress)!!
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
