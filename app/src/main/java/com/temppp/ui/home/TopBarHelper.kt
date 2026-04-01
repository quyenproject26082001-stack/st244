package com.temppp.ui.home

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.temppp.R
import com.temppp.databinding.LayoutTopBarBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun Fragment.initTopBar(topBar: LayoutTopBarBinding) {
    val cornerRadius = 20 * resources.displayMetrics.density

    topBar.coinBarContainer.outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
        }
    }
    topBar.coinBarContainer.clipToOutline = true

    val boostOutline = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
        }
    }
    topBar.progressBoostClickContainer.outlineProvider = boostOutline
    topBar.progressBoostClickContainer.clipToOutline = true
    topBar.progressBoostSecondContainer.outlineProvider = boostOutline
    topBar.progressBoostSecondContainer.clipToOutline = true

    topBar.progressCoins.pivotX = 0f
    topBar.progressCoins.scaleX = 0f

    topBar.whiteOverlayClick.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
        v.pivotX = v.width.toFloat()
    }
    topBar.whiteOverlaySecond.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
        v.pivotX = v.width.toFloat()
    }
}

fun Fragment.observeTopBar(topBar: LayoutTopBarBinding, viewModel: HomeViewModel) {
    viewLifecycleOwner.lifecycleScope.launch {
        launch {
            viewModel.coins.collectLatest { coins ->
                val milestone = nextMilestone(coins)
                topBar.tvCoinCount.text = "$coins / $milestone"
                topBar.progressCoins.scaleX = (coins.toFloat() / milestone).coerceIn(0f, 1f)
            }
        }
        launch {
            viewModel.coinsPerClick.collectLatest { cpc ->
                topBar.tvCoinsPerClick.text = formatCoins(cpc)
            }
        }
        launch {
            viewModel.coinsPerSecond.collectLatest { cps ->
                topBar.tvCoinsPerSecond.text = formatCoins(cps)
            }
        }
        launch {
            viewModel.clickBoostActive.collectLatest { active ->
                topBar.tvCoinsPerClick.setTextColor(
                    if (active) requireContext().getColor(R.color.white) else 0xFF2ECC71.toInt()
                )
            }
        }
        launch {
            viewModel.boostProgress.collectLatest { progress ->
                val visible = if (progress > 0f) View.VISIBLE else View.GONE
                topBar.layoutClickBoostClickActive.visibility = visible
                topBar.icIncreaseClick.visibility = visible
                topBar.whiteOverlayClick.scaleX = 1f - progress
            }
        }
        launch {
            viewModel.passiveBoostProgress.collectLatest { progress ->
                val visible = if (progress > 0f) View.VISIBLE else View.GONE
                topBar.layoutClickBoostActiveSecond.visibility = visible
                topBar.icIncreaseSecond.visibility = visible
                topBar.whiteOverlaySecond.scaleX = 1f - progress
            }
        }
    }
}

fun AppCompatActivity.initTopBar(topBar: LayoutTopBarBinding) {
    val cornerRadius = 20 * resources.displayMetrics.density

    topBar.coinBarContainer.outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
        }
    }
    topBar.coinBarContainer.clipToOutline = true

    val boostOutline = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
        }
    }
    topBar.progressBoostClickContainer.outlineProvider = boostOutline
    topBar.progressBoostClickContainer.clipToOutline = true
    topBar.progressBoostSecondContainer.outlineProvider = boostOutline
    topBar.progressBoostSecondContainer.clipToOutline = true

    topBar.progressCoins.pivotX = 0f
    topBar.progressCoins.scaleX = 0f

    topBar.whiteOverlayClick.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
        v.pivotX = v.width.toFloat()
    }
    topBar.whiteOverlaySecond.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
        v.pivotX = v.width.toFloat()
    }
}

fun AppCompatActivity.observeTopBar(topBar: LayoutTopBarBinding, viewModel: HomeViewModel) {
    lifecycleScope.launch {
        launch {
            viewModel.coins.collectLatest { coins ->
                val milestone = nextMilestone(coins)
                topBar.tvCoinCount.text = "$coins / $milestone"
                topBar.progressCoins.scaleX = (coins.toFloat() / milestone).coerceIn(0f, 1f)
            }
        }
        launch {
            viewModel.coinsPerClick.collectLatest { cpc ->
                topBar.tvCoinsPerClick.text = formatCoins(cpc)
            }
        }
        launch {
            viewModel.coinsPerSecond.collectLatest { cps ->
                topBar.tvCoinsPerSecond.text = formatCoins(cps)
            }
        }
        launch {
            viewModel.clickBoostActive.collectLatest { active ->
                topBar.tvCoinsPerClick.setTextColor(
                    if (active) getColor(R.color.white) else 0xFF2ECC71.toInt()
                )
            }
        }
        launch {
            viewModel.boostProgress.collectLatest { progress ->
                val visible = if (progress > 0f) View.VISIBLE else View.GONE
                topBar.layoutClickBoostClickActive.visibility = visible
                topBar.icIncreaseClick.visibility = visible
                topBar.whiteOverlayClick.scaleX = 1f - progress
            }
        }
        launch {
            viewModel.passiveBoostProgress.collectLatest { progress ->
                val visible = if (progress > 0f) View.VISIBLE else View.GONE
                topBar.layoutClickBoostActiveSecond.visibility = visible
                topBar.icIncreaseSecond.visibility = visible
                topBar.whiteOverlaySecond.scaleX = 1f - progress
            }
        }
    }
}

private fun nextMilestone(coins: Long): Long {
    var m = 1_000L
    while (m <= coins) m *= 10
    return m
}
