package com.ponymaker.avatarcreator.maker.core.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/**
 * White mask that covers ivLoading on splash screen.
 * Shrinks from left to right (left edge moves right) revealing the loading bar underneath.
 * The left edge has concave (inward) corners of 12dp radius.
 *
 * progress = 0f → fully covering
 * progress = 1f → fully retracted (nothing drawn)
 */
class LoadingMaskView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }

    private val path = Path()

    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            if (field >= 0.95f) visibility = INVISIBLE else invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        if (progress >= 1f) return

        val left = width.toFloat() * progress
        val right = width.toFloat()
        val bottom = height.toFloat()
        val available = right - left

        val base = 12f * resources.displayMetrics.density
        val r = minOf(base, available / 2f, bottom / 2f)
        val depth = minOf(base, available)

        path.rewind()

        path.moveTo(left, 0f)
        path.cubicTo(
            left + depth, 0f,
            left + depth, bottom,
            left, bottom
        )

        // bottom-right rounded corner
        path.lineTo(right - r, bottom)
        path.arcTo(right - 2 * r, bottom - 2 * r, right, bottom, 90f, -90f, false)

        // top-right rounded corner
        path.lineTo(right, r)
        path.arcTo(right - 2 * r, 0f, right, 2 * r, 0f, -90f, false)

        path.close()

        canvas.drawPath(path, paint)
    }
}
