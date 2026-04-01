package com.temppp.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.temppp.R
import com.temppp.data.local.entity.CatContact
import com.temppp.databinding.ItemCatContactBinding
import com.temppp.ui.home.formatCoins

class ContactsAdapter(
    private val onCatClick: (CatContact) -> Unit
) : ListAdapter<CatContact, ContactsAdapter.ViewHolder>(DIFF) {

    var currentCoins: Long = 0
        set(value) {
            val old = field
            field = value
            currentList.forEachIndexed { index, cat ->
                if (!cat.isUnlocked && !cat.isSelected) {
                    val wasAffordable = old >= cat.price
                    val isAffordable = value >= cat.price
                    if (wasAffordable != isAffordable) notifyItemChanged(index)
                }
            }
        }

    inner class ViewHolder(private val binding: ItemCatContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cat: CatContact) {
            val context = binding.root.context

            val imageName = if (cat.isSelected) "cat${cat.id}_light" else "cat${cat.id}_dark"
            val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
            binding.imgCat.setImageResource(if (resId != 0) resId else R.drawable.ic_loading)

            when {
                cat.isSelected -> {
                    binding.tvActionLabel.text = "CHOSEN"
                    binding.btnAction.setBackgroundResource(R.drawable.bg_price_btn)
                    binding.ivCoinIcon.visibility = android.view.View.GONE
                }
                cat.isUnlocked -> {
                    binding.tvActionLabel.text = "CHOOSE"
                    binding.btnAction.setBackgroundResource(R.drawable.bg_price_btn)
                    binding.ivCoinIcon.visibility = android.view.View.GONE
                }
                else -> {
                    binding.tvActionLabel.text = formatCoins(cat.price)
                    binding.btnAction.setBackgroundResource(
                        if (currentCoins >= cat.price) R.drawable.bg_price_btn else R.drawable.bg_price_btn_uslt
                    )
                    binding.ivCoinIcon.visibility = android.view.View.VISIBLE
                }
            }

            binding.btnAction.setOnClickListener { onCatClick(cat) }
            binding.root.setOnClickListener { onCatClick(cat) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCatContactBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CatContact>() {
            override fun areItemsTheSame(a: CatContact, b: CatContact) = a.id == b.id
            override fun areContentsTheSame(a: CatContact, b: CatContact) = a == b
        }
    }
}
