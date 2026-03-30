package com.ponymaker.avatarcreator.maker.ui.contacts

import android.view.LayoutInflater
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.ponymaker.avatarcreator.maker.core.base.BaseActivity
import com.ponymaker.avatarcreator.maker.core.extensions.handleBackLeftToRight
import com.ponymaker.avatarcreator.maker.core.extensions.tap
import com.ponymaker.avatarcreator.maker.data.local.DatabaseProvider
import com.ponymaker.avatarcreator.maker.databinding.ActivityContactsBinding
import com.ponymaker.avatarcreator.maker.ui.home.formatCoins
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ContactsActivity : BaseActivity<ActivityContactsBinding>() {

    private lateinit var viewModel: ContactsViewModel
    private lateinit var adapter: ContactsAdapter

    override fun setViewBinding(): ActivityContactsBinding =
        ActivityContactsBinding.inflate(LayoutInflater.from(this))

    override fun initView() {
        viewModel = ViewModelProvider.AndroidViewModelFactory
            .getInstance(application)
            .create(ContactsViewModel::class.java)

        adapter = ContactsAdapter { cat ->
            viewModel.tryUnlockAndSelect(cat) {
                showToast("Not enough coins!")
            }
        }
        binding.rvContacts.layoutManager = GridLayoutManager(this, 2)
        binding.rvContacts.adapter = adapter

        loadCoinStats()
    }

    override fun viewListener() {
        binding.btnClose.tap { handleBackLeftToRight() }
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            viewModel.cats.collectLatest { cats ->
                adapter.submitList(cats)
            }
        }
    }

    override fun initActionBar() {}

    override fun shouldPlayBackgroundMusic(): Boolean = true

    private fun loadCoinStats() {
        lifecycleScope.launch {
            val db = DatabaseProvider.getDatabase(this@ContactsActivity)
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
