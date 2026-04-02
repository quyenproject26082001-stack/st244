package com.temppp.ui.home


import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.Outline
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.temppp.R
import android.net.Uri
import androidx.core.app.ActivityCompat.finishAffinity
import com.bumptech.glide.Glide
import com.temppp.core.base.BaseFragment
import com.temppp.core.extensions.tap
import com.temppp.core.helper.RateHelper
import com.temppp.core.helper.SharePreferenceHelper
import com.temppp.core.utils.state.RateState
import com.temppp.databinding.ActivityHomeBinding
import com.temppp.dialog.YesNoDialog
import com.temppp.ui.MainActivity
import com.temppp.ui.call.CallActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.rem


class HomeFragment : BaseFragment<ActivityHomeBinding>() {

    val viewModel: HomeViewModel by activityViewModels()

    private var clickBoostAnim: AnimatorSet? = null
    private var passiveBoostAnim: AnimatorSet? = null
    private var clickX2Anim: AnimatorSet? = null
    private var passiveX2Anim: AnimatorSet? = null
    private var clickSpeedLinesJob: Job? = null
    private var passiveSpeedLinesJob: Job? = null
    private var limitBlinkAnim: AnimatorSet? = null

    override fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ActivityHomeBinding.inflate(inflater, container, false)

    override fun initView() {

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
                shakePhoneButton()
                spawnFloatingLabel(viewModel.currentClickValue())
            }
            btnClickBoost.tap(500) {
                showBoostDialog(R.string.ads_click) { viewModel.activateClickBoost() }
            }
            btnPassiveBoost.tap(500) {
                showBoostDialog(R.string.ads_second) { viewModel.activatePassiveBoost() }
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
            btnPhone.tap(300) {
                val catId = viewModel.selectedCat.value?.id ?: 1
                CallActivity.start(requireActivity(), catId)
            }
        }
    }

    override fun dataObservable() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.coins.collectLatest { coins -> updateCoinDisplay(coins) }
            }
            launch {
                viewModel.coinLimit.collectLatest { updateCoinDisplay(viewModel.coins.value) }
            }
            launch {
                combine(viewModel.coinsPerClick, viewModel.clickBoostActive) { cpc, boostActive ->
                    if (boostActive) cpc * 2 else cpc
                }.collectLatest { effectiveCpc ->
                    binding.tvCoinsPerClick.text = "${formatCoins(effectiveCpc)}"
                }
            }
            launch {
                combine(viewModel.coinsPerSecond, viewModel.passiveBoostActive) { cps, boostActive ->
                    if (boostActive) cps * 2 else cps
                }.collectLatest { effectiveCps ->
                    binding.tvCoinsPerSecond.text = "${formatCoins(effectiveCps)}"
                }
            }
            launch {
                while (isActive) {
                    delay(1_000)
                    if (viewModel.coinsPerSecond.value > 0 && viewModel.coins.value < viewModel.coinLimit.value) {
                        val effectiveCps = viewModel.coinsPerSecond.value * if (viewModel.passiveBoostActive.value) 2L else 1L
                        spawnFloatingLabel(effectiveCps)
                    }
                }
            }
            launch {
                var wasActive = false
                viewModel.boostProgress.collectLatest { progress ->
                    val isActive = progress > 0f
                    val visible = if (isActive) View.VISIBLE else View.GONE
                    binding.layoutClickBoostClickActive.visibility = visible
                    binding.icIncreaseClick.visibility = visible
                    binding.whiteOverlayClick.scaleX = 1f - progress
                    if (isActive && !wasActive) {
                        clickBoostAnim = startIncreaseAnimation(binding.icIncreaseClick)
                        clickSpeedLinesJob = startSpeedLinesLoop(binding.icIncreaseClick)
                        clickX2Anim = startX2PulseAnimation(binding.icX2Click)
                    } else if (!isActive && wasActive) {
                        stopIncreaseAnimation(clickBoostAnim, binding.icIncreaseClick)
                        clickBoostAnim = null
                        clickSpeedLinesJob?.cancel()
                        clickSpeedLinesJob = null
                        clickX2Anim?.cancel(); clickX2Anim = null
                        binding.icX2Click.apply { scaleX = 1f; scaleY = 1f; alpha = 1f }
                    }
                    wasActive = isActive
                }
            }
            launch {
                var wasActive = false
                viewModel.passiveBoostProgress.collectLatest { progress ->
                    val isActive = progress > 0f
                    val visible = if (isActive) View.VISIBLE else View.GONE
                    binding.layoutClickBoostActiveSecond.visibility = visible
                    binding.icIncreaseSecond.visibility = visible
                    binding.whiteOverlaySecond.scaleX = 1f - progress
                    if (isActive && !wasActive) {
                        passiveBoostAnim = startIncreaseAnimation(binding.icIncreaseSecond)
                        passiveSpeedLinesJob = startSpeedLinesLoop(binding.icIncreaseSecond)
                        passiveX2Anim = startX2PulseAnimation(binding.icX2Second)
                    } else if (!isActive && wasActive) {
                        stopIncreaseAnimation(passiveBoostAnim, binding.icIncreaseSecond)
                        passiveBoostAnim = null
                        passiveSpeedLinesJob?.cancel()
                        passiveSpeedLinesJob = null
                        passiveX2Anim?.cancel(); passiveX2Anim = null
                        binding.icX2Second.apply { scaleX = 1f; scaleY = 1f; alpha = 1f }
                    }
                    wasActive = isActive
                }
            }
            launch {
                viewModel.tapProgress.collectLatest { progress ->
                    binding.whiteOverlay.scaleX = 1f - progress
                }
            }
            launch {
                viewModel.selectedCat.collectLatest { cat ->
                    if (cat != null) loadCatImage(cat.id)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveCoins()
    }

    private fun updateCoinDisplay(coins: Long) {
        val limit = viewModel.coinLimit.value
        val atLimit = limit != Long.MAX_VALUE && coins >= limit
        val milestone = if (limit != Long.MAX_VALUE) limit else nextMilestone(coins)
        binding.tvCoinCount.text = "${formatCoins(coins)} / ${formatCoins(milestone)}"
        binding.progressCoins.scaleX = (coins.toFloat() / milestone).coerceIn(0f, 1f)
        binding.progressCoins.setBackgroundColor(Color.parseColor("#FFDD33"))
        binding.catFrame.isEnabled = !atLimit
        binding.catFrame.alpha = if (atLimit) 0.5f else 1f

        if (atLimit && limitBlinkAnim == null) {
            val colorAnim = android.animation.ValueAnimator.ofArgb(
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#FF4444")
            ).apply {
                duration = 500
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = android.animation.ValueAnimator.REVERSE
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { binding.tvCoinCount.setTextColor(it.animatedValue as Int) }
            }
            limitBlinkAnim = AnimatorSet().also { it.play(colorAnim); it.start() }
        } else if (!atLimit && limitBlinkAnim != null) {
            limitBlinkAnim?.cancel(); limitBlinkAnim = null
            binding.tvCoinCount.setTextColor(Color.parseColor("#FFFFFF"))
        }
    }

    private fun nextMilestone(coins: Long): Long {
        var m = 1_000L
        while (m <= coins) m *= 10
        return m
    }

    private fun shakePhoneButton() {
        val dp = resources.displayMetrics.density
        val s = 6f * dp
        ObjectAnimator.ofFloat(binding.btnPhone, View.TRANSLATION_X,
            0f, -s, s, -s * 0.6f, s * 0.6f, -s * 0.3f, s * 0.3f, 0f
        ).apply {
            duration = 320
            interpolator = LinearInterpolator()
            start()
        }
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

    private val floatColors = intArrayOf(
        0xFFFFDD33.toInt(), // yellow
        0xFFFF6B6B.toInt(), // red-pink
        0xFF6BFFB8.toInt(), // mint green
        0xFF6BC5FF.toInt(), // sky blue
        0xFFFF9F40.toInt(), // orange
        0xFFD96BFF.toInt(), // purple
        0xFFFF6BE8.toInt(), // hot pink
        0xFFFFFFFF.toInt(), // white
    )

    private fun spawnFloatingLabel(amount: Long) {
        val rootView = binding.root
        val density = resources.displayMetrics.density

        val catLoc = IntArray(2)
        binding.catFrame.getLocationInWindow(catLoc)
        val rootLoc = IntArray(2)
        rootView.getLocationInWindow(rootLoc)

        val frameLeft  = catLoc[0] - rootLoc[0]
        val frameTop   = catLoc[1] - rootLoc[1]
        val frameW     = binding.catFrame.width.toFloat()
        val frameH     = binding.catFrame.height.toFloat()

        val color = floatColors.random()
        val label = TextView(requireContext()).apply {
            text = "+${formatCoins(amount)}"
            textSize = (14f + Math.random().toFloat() * 10f)
            typeface = ResourcesCompat.getFont(requireContext(), R.font.cherry_bomb_regular)
            setTextColor(color)
            setShadowLayer(6f, 0f, 2f, Color.argb(180, 0, 0, 0))
        }

        rootView.addView(label)
        label.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        // Spawn anywhere inside the catFrame circle
        val spawnX = frameLeft + (Math.random() * frameW).toFloat()
        val spawnY = frameTop  + (Math.random() * frameH).toFloat()
        label.x = spawnX - label.measuredWidth / 2f
        label.y = spawnY - label.measuredHeight / 2f

        // Drift horizontally ±40dp, float up 80–140dp
        val driftX = ((Math.random() - 0.5) * 80 * density).toFloat()
        val floatY = (80f + Math.random().toFloat() * 60f) * density

        label.animate()
            .translationXBy(driftX)
            .translationYBy(-floatY)
            .alpha(0f)
            .setDuration(700 + (Math.random() * 400).toLong())
            .withEndAction { rootView.removeView(label) }
            .start()
    }

    // ── x2 badge pulse ──────────────────────────────────────────────────────

    private fun startX2PulseAnimation(badge: View): AnimatorSet {
        val scaleUp = ObjectAnimator.ofFloat(badge, View.SCALE_X, 1f, 1.35f, 1f).apply {
            duration = 500; repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
        val scaleUpY = ObjectAnimator.ofFloat(badge, View.SCALE_Y, 1f, 1.35f, 1f).apply {
            duration = 500; repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
        val glow = ObjectAnimator.ofFloat(badge, View.ALPHA, 1f, 0.5f, 1f).apply {
            duration = 500; repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
        return AnimatorSet().also {
            it.playTogether(scaleUp, scaleUpY, glow)
            it.start()
        }
    }

    // ── Increase icon animation ──────────────────────────────────────────────

    private fun startIncreaseAnimation(icon: View): AnimatorSet {
        val dp = resources.displayMetrics.density
        val floatDist = 4f * dp

        // Float: Y oscillates up → down → up
        val floatY = ObjectAnimator.ofFloat(icon, View.TRANSLATION_Y, 0f, -floatDist, 0f, floatDist, 0f).apply {
            duration = 480
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
        }
        // Squash & stretch: inverse scaleX/scaleY tied to float phase
        val sX = ObjectAnimator.ofFloat(icon, View.SCALE_X, 1f, 0.75f, 1f, 1.25f, 1f).apply {
            duration = 480
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
        }
        val sY = ObjectAnimator.ofFloat(icon, View.SCALE_Y, 1f, 1.25f, 1f, 0.75f, 1f).apply {
            duration = 480
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
        }
        // Pulse: alpha flicker faster than float for visual excitement
        val pulse = ObjectAnimator.ofFloat(icon, View.ALPHA, 1f, 0.55f, 1f).apply {
            duration = 320
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        return AnimatorSet().also {
            it.playTogether(floatY, sX, sY, pulse)
            it.start()
        }
    }

    private fun stopIncreaseAnimation(anim: AnimatorSet?, icon: View) {
        anim?.cancel()
        icon.translationY = 0f
        icon.scaleX = 1f
        icon.scaleY = 1f
        icon.alpha = 1f
    }

    private fun startSpeedLinesLoop(icon: View): Job =
        viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                if (icon.isAttachedToWindow && icon.visibility == View.VISIBLE) {
                    spawnSpeedLines(icon)
                }
                delay(130)
            }
        }

    private val speedLineColors = intArrayOf(
        0xFFFFDD33.toInt(), 0xFFFFFFFF.toInt(),
        0xFFFF9F40.toInt(), 0xFFFF6B6B.toInt(),
        0xFF6BC5FF.toInt(), 0xFFD96BFF.toInt(),
    )

    private fun spawnSpeedLines(icon: View) {
        val rootView = binding.root
        val dp = resources.displayMetrics.density

        val iconLoc = IntArray(2)
        icon.getLocationInWindow(iconLoc)
        val rootLoc = IntArray(2)
        rootView.getLocationInWindow(rootLoc)
        val cx = iconLoc[0] - rootLoc[0] + icon.width / 2f
        val cy = iconLoc[1] - rootLoc[1] + icon.height / 2f

        repeat(4) {
            val lineW = ((8 + Math.random() * 16) * dp).toInt()
            val lineH = (2f * dp).toInt().coerceAtLeast(2)
            val line = View(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(lineW, lineH)
                setBackgroundColor(speedLineColors.random())
                alpha = 0.9f
                rotation = (Math.random() * 360).toFloat()
            }
            rootView.addView(line)
            line.x = cx - lineW / 2f
            line.y = cy - lineH / 2f

            val angle = Math.random() * 2 * Math.PI
            val dist = ((14 + Math.random() * 22) * dp).toFloat()
            line.animate()
                .translationXBy((Math.cos(angle) * dist).toFloat())
                .translationYBy((Math.sin(angle) * dist).toFloat())
                .alpha(0f)
                .setDuration(200)
                .withEndAction { if (line.parent != null) rootView.removeView(line) }
                .start()
        }
    }

    private fun showBoostDialog(descriptionRes: Int, onConfirm: () -> Unit) {
        val dialog = YesNoDialog(requireActivity(), R.string.what_ads, descriptionRes)
        dialog.onYesClick = { dialog.dismiss(); onConfirm() }
        dialog.onNoClick = { dialog.dismiss() }
        dialog.onDismissClick = { dialog.dismiss() }
        dialog.show()
    }

    private fun loadCatImage(catId: Int) {
        val assetFile = if (catId == 1) "avatar/img.png" else "avatar/img_${catId - 1}.png"
        Glide.with(this)
            .load(Uri.parse("file:///android_asset/$assetFile"))
            .placeholder(R.drawable.ic_loading)
            .into(binding.imgCat)
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val newCount = sharePreference.getCountBack() + 1
                sharePreference.setCountBack(newCount)
                if (!sharePreference.getIsRate(requireContext()) && newCount % 2 == 0) {
                    RateHelper.showRateDialog(requireActivity(), sharePreference)
                }
            }
        })
    }
}
