package com.ponymaker.avatarcreator.maker.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ponymaker.avatarcreator.maker.R
import com.ponymaker.avatarcreator.maker.data.local.entity.CatContact
import com.ponymaker.avatarcreator.maker.databinding.ItemCatContactBinding
import com.ponymaker.avatarcreator.maker.ui.home.formatCoins

class ContactsAdapter(
    private val onCatClick: (CatContact) -> Unit
) : ListAdapter<CatContact, ContactsAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemCatContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cat: CatContact) {
            val context = binding.root.context

            val resId = context.resources.getIdentifier(cat.imageRes, "drawable", context.packageName)
            binding.imgCat.setImageResource(if (resId != 0) resId else R.drawable.ic_loading)

            when {
                cat.isSelected -> {
                    binding.btnAction.text = "CHOSEN"
                    binding.btnAction.setBackgroundResource(R.drawable.bg_chosen_btn)
                }
                cat.isUnlocked -> {
                    binding.btnAction.text = "CHOOSE"
                    binding.btnAction.setBackgroundResource(R.drawable.bg_price_btn)
                }
                else -> {
                    binding.btnAction.text = "${formatCoins(cat.price)} \uD83E\uDE99"
                    binding.btnAction.setBackgroundResource(R.drawable.bg_price_btn)
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
