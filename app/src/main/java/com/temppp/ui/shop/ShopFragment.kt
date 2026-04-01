package com.temppp.ui.shop

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.temppp.core.base.BaseFragment
import com.temppp.core.extensions.tap
import com.temppp.databinding.ActivityShopBinding
import com.temppp.ui.home.HomeViewModel
import com.temppp.ui.home.initTopBar
import com.temppp.ui.home.observeTopBar
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
        initTopBar(binding.topBar)
    }

    override fun viewListener() {
        binding.btnClose.tap { parentFragmentManager.popBackStack() }
    }

    override fun dataObservable() {
        observeTopBar(binding.topBar, viewModel)
        viewLifecycleOwner.lifecycleScope.launch {
            launch { viewModel.upgrades.collectLatest { adapter.submitList(it) } }
            launch { viewModel.coins.collectLatest { adapter.currentCoins = it } }
        }
    }
}
