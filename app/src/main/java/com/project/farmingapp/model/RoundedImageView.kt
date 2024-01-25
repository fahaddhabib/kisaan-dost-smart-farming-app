package com.project.farmingapp.model

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.project.farmingapp.R

class RoundedImageView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {

    private val path = Path()
    private val rect = RectF()
    private var cornerRadius = 0f
    private var borderWidth = 0f
    private var borderColor = Color.BLACK

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView)
        cornerRadius = typedArray.getDimension(R.styleable.RoundedImageView_cornerRadius, 0f)
        borderWidth = typedArray.getDimension(R.styleable.RoundedImageView_borderWidth, 0f)
        borderColor = typedArray.getColor(R.styleable.RoundedImageView_borderColor, Color.BLACK)
        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas?) {
        path.reset()
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)

        canvas?.clipPath(path)

        super.onDraw(canvas)

        if (borderWidth > 0) {
            val paint = Paint()
            paint.color = borderColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = borderWidth
            canvas?.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        }
    }
}
