package com.cat.cute.callthecat.core.custom.drawable

import android.graphics.*
import android.graphics.drawable.Drawable

/**
 * Wraps any Drawable và clip nó thành hình pill (bo góc = height/2).
 * Dùng canvas.clipPath() ở software level — hoạt động đồng nhất trên mọi thiết bị Android.
 */
class PillDrawable(private val inner: Drawable) : Drawable() {

    private val path = Path()

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        inner.bounds = bounds
        val radius = bounds.height() / 2f
        path.reset()
        path.addRoundRect(RectF(bounds), radius, radius, Path.Direction.CW)
    }

    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.clipPath(path)
        inner.draw(canvas)
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {
        inner.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        inner.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}
