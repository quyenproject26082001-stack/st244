package com.ponymaker.avatarcreator.maker.ui.shop

import android.view.LayoutInflater
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ponymaker.avatarcreator.maker.core.base.BaseActivity
import com.ponymaker.avatarcreator.maker.core.extensions.handleBackLeftToRight
import com.ponymaker.avatarcreator.maker.core.extensions.tap
import com.ponymaker.avatarcreator.maker.data.local.DatabaseProvider
import com.ponymaker.avatarcreator.maker.databinding.ActivityShopBinding
import com.ponymaker.avatarcreator.maker.ui.home.formatCoins
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ShopActivity : BaseActivity<ActivityShopBinding>() {

    private lateinit var viewModel: ShopViewModel
    private lateinit var adapter: ShopAdapter

    override fun setViewBinding(): ActivityShopBinding =
        ActivityShopBinding.inflate(LayoutInflater.from(this))

    override fun initView() {
        viewModel = ViewModelProvider.AndroidViewModelFactory
            .getInstance(application)
            .create(ShopViewModel::class.java)

        adapter = ShopAdapter { upgrade ->
            viewModel.tryBuy(upgrade) {
                showToast("Not enough coins!")
            }
        }
        binding.rvShop.layoutManager = LinearLayoutManager(this)
        binding.rvShop.adapter = adapter

        loadCoinStats()
    }

    override fun viewListener() {
        binding.btnClose.tap { handleBackLeftToRight() }
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            viewModel.upgrades.collectLatest { upgrades ->
                adapter.submitList(upgrades)
            }
        }
    }

    override fun initActionBar() {}

    override fun shouldPlayBackgroundMusic(): Boolean = true

    private fun loadCoinStats() {
        lifecycleScope.launch {
            val db = DatabaseProvider.getDatabase(this@ShopActivity)
            db.gameStateDao().getGameState().collect { state ->
                if (state != null) {
                    binding.tvCoinCount.text = formatCoins(state.coins)
                    binding.tvCoinsPerClick.text = "${formatCoins(state.coinsPerClick)} /click"
                    binding.tvCoinsPerSecond.text = "${formatCoins(state.coinsPerSecond)} /second"
                }
            }
        }
    }
}
