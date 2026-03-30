package com.ponymaker.avatarcreator.maker.ui.shop

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ponymaker.avatarcreator.maker.core.base.BaseFragment
import com.ponymaker.avatarcreator.maker.core.extensions.tap
import com.ponymaker.avatarcreator.maker.databinding.ActivityShopBinding
import com.ponymaker.avatarcreator.maker.ui.home.HomeViewModel
import com.ponymaker.avatarcreator.maker.ui.home.formatCoins
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
    }

    override fun viewListener() {
        binding.btnClose.tap { parentFragmentManager.popBackStack() }
    }

    override fun dataObservable() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.upgrades.collectLatest { adapter.submitList(it) }
            }
            launch {
                viewModel.coins.collectLatest { binding.tvCoinCount.text = formatCoins(it) }
            }
            launch {
                viewModel.coinsPerClick.collectLatest {
                    binding.tvCoinsPerClick.text = "${formatCoins(it)} /click"
                }
            }
            launch {
                viewModel.coinsPerSecond.collectLatest {
                    binding.tvCoinsPerSecond.text = "${formatCoins(it)} /second"
                }
            }
        }
    }
}
