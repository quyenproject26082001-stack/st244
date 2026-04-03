package com.cat.cute.callthecat.core.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.cat.cute.callthecat.core.helper.LanguageHelper
import com.cat.cute.callthecat.core.helper.SharePreferenceHelper

abstract class BaseFragment<T : ViewBinding> : Fragment() {

    protected lateinit var binding: T

    protected val sharePreference: SharePreferenceHelper by lazy {
        SharePreferenceHelper(requireContext())
    }

    fun showToast(content: Any) {
        val msg = when (content) {
            is String -> content
            is Int -> getString(content)
            else -> ""
        }
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    protected abstract fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?): T

    protected abstract fun initView()

    protected abstract fun viewListener()

    open fun dataObservable() {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        LanguageHelper.setLocale(requireContext())
        binding = setViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        dataObservable()
        viewListener()
    }

}
