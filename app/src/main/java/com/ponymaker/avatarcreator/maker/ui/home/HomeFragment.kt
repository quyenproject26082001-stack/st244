package com.ponymaker.avatarcreator.maker.ui.home


import android.graphics.Outline
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.animation.ScaleAnimation
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.ponymaker.avatarcreator.maker.R
import com.ponymaker.avatarcreator.maker.core.base.BaseFragment
import com.ponymaker.avatarcreator.maker.core.extensions.tap
import com.ponymaker.avatarcreator.maker.databinding.ActivityHomeBinding
import com.ponymaker.avatarcreator.maker.ui.MainActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch



class HomeFragment : BaseFragment<ActivityHomeBinding>() {

    val viewModel: HomeViewModel by activityViewModels()

    override fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ActivityHomeBinding.inflate(inflater, container, false)

    override fun initView() {
        sharePreference.setCountBack(sharePreference.getCountBack() + 1)

        val cornerRadius = 20 * resources.displayMetrics.density

        // Clip progressBarContainer (tap bar)
        binding.progressBarContainer.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
            }
        }
        binding.progressBarContainer.clipToOutline = true

        // Clip coinBarContainer — progressCoins bo góc theo container, stroke foreground vẽ đè lên sau
        binding.coinBarContainer.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
            }
        }
        binding.coinBarContainer.clipToOutline = true

        // Clip boost bar containers
        val boostOutline = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, cornerRadius)
            }
        }
        binding.progressBoostClickContainer.outlineProvider = boostOutline
        binding.progressBoostClickContainer.clipToOutline = true
        binding.progressBoostSecondContainer.outlineProvider = boostOutline
        binding.progressBoostSecondContainer.clipToOutline = true

        // Coin progress fills left-to-right
        binding.progressCoins.pivotX = 0f
        binding.progressCoins.scaleX = 0f

        // Pivot white overlays at their right edge — dùng layoutChangeListener để đảm bảo đúng kể cả khi view bắt đầu GONE
        binding.whiteOverlay.post {
            binding.whiteOverlay.pivotX = binding.whiteOverlay.width.toFloat()
        }
        binding.whiteOverlayClick.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            v.pivotX = v.width.toFloat()
        }
        binding.whiteOverlaySecond.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            v.pivotX = v.width.toFloat()
        }
    }

    override fun viewListener() {
        binding.apply {
            catFrame.tap(0) {
                viewModel.onPhoneClick()
                animatePhoneButton()
            }
            btnClickBoost.tap(500) {
                viewModel.activateClickBoost()
                showToast("x2 Click boost activated!")
            }
            btnPassiveBoost.tap(500) {
                viewModel.activatePassiveBoost()
                showToast("x2 Passive boost activated!")
            }
            btnNavShop.tap(300) {
                (requireActivity() as MainActivity).navigateTo(MainActivity.TAG_SHOP)
            }
            btnNavContacts.tap(300) {
                (requireActivity() as MainActivity).navigateTo(MainActivity.TAG_CONTACTS)
            }
            btnNavSettings.tap(300) {
                (requireActivity() as MainActivity).navigateTo(MainActivity.TAG_SETTINGS)
            }
        }
    }

    override fun dataObservable() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.coins.collectLatest { coins -> updateCoinDisplay(coins) }
            }
            launch {
                viewModel.coinsPerClick.collectLatest { cpc ->
                    binding.tvCoinsPerClick.text = "${formatCoins(cpc)}"
                }
            }
            launch {
                viewModel.coinsPerSecond.collectLatest { cps ->
                    binding.tvCoinsPerSecond.text = "${formatCoins(cps)}"
                }
            }
            launch {
                viewModel.clickBoostActive.collectLatest { active ->
                    if (active) {
                        binding.tvCoinsPerClick.setTextColor(requireContext().getColor(R.color.white))
                    } else {
                        binding.tvCoinsPerClick.setTextColor(0xFF2ECC71.toInt())
                    }
                }
            }
            launch {
                viewModel.boostProgress.collectLatest { progress ->
                    val visible = if (progress > 0f) android.view.View.VISIBLE else android.view.View.GONE
                    binding.layoutClickBoostClickActive.visibility = visible
                    binding.icIncreaseClick.visibility = visible
                    binding.whiteOverlayClick.scaleX = 1f - progress
                }
            }
            launch {
                viewModel.passiveBoostProgress.collectLatest { progress ->
                    val visible = if (progress > 0f) android.view.View.VISIBLE else android.view.View.GONE
                    binding.layoutClickBoostActiveSecond.visibility = visible
                    binding.icIncreaseSecond.visibility = visible
                    binding.whiteOverlaySecond.scaleX = 1f - progress
                }
            }
            launch {
                viewModel.tapProgress.collectLatest { progress ->
                    binding.whiteOverlay.scaleX = 1f - progress
                }
            }
            launch {
                viewModel.selectedCat.collectLatest { cat ->
                    if (cat != null) loadCatImage(cat.imageRes)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveCoins()
    }

    private fun updateCoinDisplay(coins: Long) {
        val milestone = nextMilestone(coins)
        binding.tvCoinCount.text = "$coins / $milestone"
        binding.progressCoins.scaleX = (coins.toFloat() / milestone).coerceIn(0f, 1f)
    }

    private fun nextMilestone(coins: Long): Long {
        var m = 1_000L
        while (m <= coins) m *= 10
        return m
    }

    private fun animatePhoneButton() {
        val scaleAnim = ScaleAnimation(
            1f, 0.85f, 1f, 0.85f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        )
        scaleAnim.duration = 80
        scaleAnim.repeatCount = 1
        scaleAnim.repeatMode = android.view.animation.Animation.REVERSE
        binding.catFrame.startAnimation(scaleAnim)
    }

    private fun loadCatImage(imageRes: String) {
        val resId = resources.getIdentifier(imageRes, "drawable", requireContext().packageName)
        binding.imgCat.setImageResource(if (resId != 0) resId else R.drawable.ic_loading)
    }
}
