package com.temppp.dialog

import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Color
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.temppp.core.extensions.gone
import com.temppp.core.extensions.hideNavigation
import com.temppp.core.extensions.tap
import com.temppp.R
import com.temppp.core.base.BaseDialog
import com.temppp.core.extensions.strings
import com.temppp.core.extensions.visible
import com.temppp.databinding.DialogConfirmBinding

enum class DialogType {
    DELETE_EXIT,
    RESET,
    LOADING,
    INTERNET,
    PERMISSION
}

class YesNoDialog(
    val context: Activity,
    val title: Int,
    val description: Int,
    val isError: Boolean = false,
    val dialogType: DialogType = DialogType.DELETE_EXIT
) : BaseDialog<DialogConfirmBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_confirm
    override val isCancelOnTouchOutside: Boolean = false
    override val isCancelableByBack: Boolean = false

    var onNoClick: (() -> Unit) = {}
    var onYesClick: (() -> Unit) = {}
    var onDismissClick: (() -> Unit) = {}

    private var spinAnim: ObjectAnimator? = null

    override fun initView() {
        initText()
        initBackground()
        if (isError) {
            binding.btnNo.gone()
        }
        context.hideNavigation()
        binding.tvTitle.isSelected = true
    }

    private fun initBackground() {
        binding.containerDialog.setBackgroundResource(R.drawable.bg_dialog)
        binding.tvDescription.setTextColor(Color.parseColor("#00407F"))
        binding.btnNo.setBackgroundResource(R.drawable.bg_btn_no)
        binding.btnYes.setBackgroundResource(R.drawable.bg_btn_yes)

        when (dialogType) {
            DialogType.LOADING -> {
                binding.btnNo.gone()
                (binding.btnYes.layoutParams as LinearLayout.LayoutParams).marginStart = 0

                binding.spin.visible()
                spinAnim = ObjectAnimator.ofFloat(binding.spin, "rotation", 0f, 360f).apply {
                    duration = 1000
                    repeatCount = ObjectAnimator.INFINITE
                    interpolator = LinearInterpolator()
                    start()
                }

                val cs = ConstraintSet()
                cs.clone(binding.containerDialog as ConstraintLayout)
                cs.clear(R.id.tvDescription, ConstraintSet.TOP)
                cs.connect(
                    R.id.tvDescription, ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM,
                    (8 * context.resources.displayMetrics.density).toInt()
                )
                cs.applyTo(binding.containerDialog as ConstraintLayout)
            }
            DialogType.INTERNET -> {
                binding.btnNo.gone()
                binding.btnYes.setBackgroundResource(R.drawable.bg_btn_internet)
                (binding.btnYes.layoutParams as LinearLayout.LayoutParams).marginStart = 0
            }
            else -> {}
        }
    }

    override fun initAction() {
        binding.apply {
            btnNo.tap { onNoClick.invoke() }
            btnYes.tap { onYesClick.invoke() }
            flOutSide.tap { onDismissClick.invoke() }
        }
    }

    override fun onDismissListener() {
        spinAnim?.cancel()
        spinAnim = null
    }

    private fun initText() {
        binding.apply {
            tvTitle.text = context.strings(title)
            tvDescription.text = context.strings(description)
            if (isError) {
                btnYes.text = context.strings(R.string.ok)
            }
        }
    }
}
