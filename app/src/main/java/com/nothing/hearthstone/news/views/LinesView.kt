package com.nothing.hearthstone.news.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class LinesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B0B0B0")
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        val rows = 6
        val cols = 8
        val spacingX = w / (cols + 1)
        val spacingY = h / (rows + 1)
        val radius = (Math.min(spacingX, spacingY) * 0.18f).coerceAtLeast(2f)
        for (r in 1..rows) {
            for (c in 1..cols) {
                if (c > cols - (r % 3)) continue
                val cx = c * spacingX
                val cy = r * spacingY
                canvas.drawCircle(cx, cy, radius, dotPaint)
            }
        }
    }
}