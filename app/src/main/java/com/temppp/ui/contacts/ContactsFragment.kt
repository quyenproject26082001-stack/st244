package com.temppp.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.temppp.core.base.BaseFragment
import com.temppp.databinding.ActivityContactsBinding
import com.temppp.core.extensions.tap
import com.temppp.ui.home.HomeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ContactsFragment : BaseFragment<ActivityContactsBinding>() {

    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var adapter: ContactsAdapter

    override fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ActivityContactsBinding.inflate(inflater, container, false)

    override fun initView() {
        adapter = ContactsAdapter { cat ->
            viewModel.tryUnlockAndSelectCat(cat) { showToast("Not enough coins!") }
        }
        binding.rvContacts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvContacts.adapter = adapter
    }

    override fun viewListener() {
        binding.btnClose.tap { parentFragmentManager.popBackStack() }
    }

    override fun dataObservable() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch { viewModel.cats.collectLatest { adapter.submitList(it) } }
            launch { viewModel.coins.collectLatest { adapter.currentCoins = it } }
        }
    }
}
