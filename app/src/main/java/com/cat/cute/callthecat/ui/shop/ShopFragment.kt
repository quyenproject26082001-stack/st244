package com.cat.cute.callthecat.ui.shop

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cat.cute.callthecat.core.base.BaseFragment
import com.cat.cute.callthecat.databinding.ActivityShopBinding
import com.cat.cute.callthecat.core.extensions.tap
import com.cat.cute.callthecat.ui.home.HomeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ShopFragment : BaseFragment<ActivityShopBinding>() {

    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var adapter: ShopAdapter

    override fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ActivityShopBinding.inflate(inflater, container, false)

    override fun initView() {
        adapter = ShopAdapter { upgrade ->
            viewModel.buyUpgrade(upgrade) { showToast("Not enough coins!") }
        }
        binding.rvShop.layoutManager = LinearLayoutManager(requireContext())
        binding.rvShop.adapter = adapter
        binding.tvShop.isSelected =true
    }

    override fun viewListener() {
        binding.btnClose.tap { parentFragmentManager.popBackStack() }
    }

    override fun dataObservable() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch { viewModel.upgrades.collectLatest { adapter.submitList(it) } }
            launch { viewModel.coins.collectLatest { adapter.currentCoins = it } }
        }
    }
}
