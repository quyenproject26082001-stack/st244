package com.temppp.ui.contacts

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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

            // Load avatar từ assets
            val assetFile = if (cat.id == 1) "avatar/img.png" else "avatar/img_${cat.id - 1}.png"
            Glide.with(context)
                .load(Uri.parse("file:///android_asset/$assetFile"))
                .into(binding.imgCat)

            // Overlay 50% đen cho item chưa unlock
            binding.overlayDark.visibility = if (!cat.isSelected) View.VISIBLE else View.GONE

            when {
                cat.isSelected -> {
                    binding.tvActionLabel.text = context.getString(R.string.selected1)
                    binding.btnAction.setBackgroundResource(R.drawable.bg_selected)
                    binding.ivCoinIcon.visibility = View.GONE
                }
                cat.isUnlocked -> {
                    binding.tvActionLabel.text = context.getString(R.string.choose)
                    binding.btnAction.setBackgroundResource(R.drawable.bg_price_btn)
                    binding.ivCoinIcon.visibility = View.GONE
                }
                else -> {
                    binding.tvActionLabel.text = formatCoins(cat.price)
                    binding.btnAction.setBackgroundResource(
                        if (currentCoins >= cat.price) R.drawable.bg_price_btn else R.drawable.bg_price_btn_uslt
                    )
                    binding.ivCoinIcon.visibility = View.VISIBLE
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
