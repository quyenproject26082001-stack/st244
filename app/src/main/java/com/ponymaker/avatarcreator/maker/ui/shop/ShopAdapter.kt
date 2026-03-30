package com.ponymaker.avatarcreator.maker.ui.shop

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ponymaker.avatarcreator.maker.data.local.entity.ShopUpgrade
import com.ponymaker.avatarcreator.maker.databinding.ItemShopUpgradeBinding
import com.ponymaker.avatarcreator.maker.ui.home.formatCoins

class ShopAdapter(
    private val onBuyClick: (ShopUpgrade) -> Unit
) : ListAdapter<ShopUpgrade, ShopAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemShopUpgradeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(upgrade: ShopUpgrade) {
            val levelDisplay = if (upgrade.level == 0) "lv.1" else "lv.${upgrade.level + 1}"
            binding.tvLevel.text = levelDisplay
            binding.tvUpgradeName.text = "${upgrade.name} $levelDisplay"
            binding.tvUpgradeDesc.text = upgrade.description
            binding.btnBuy.text = formatCoins(upgrade.price)
            binding.btnBuy.setOnClickListener { onBuyClick(upgrade) }
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
