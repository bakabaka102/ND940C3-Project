package com.udacity

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

@SuppressLint("ResourceType")
class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    //val a = resources.getString(R.color.colorPrimary)
    private var defaultBackgroundColor = Color.parseColor(resources.getString(R.color.colorPrimary))
    private var loadingBackgroundColor =
        Color.parseColor(resources.getString(R.color.colorPrimaryDark))

    /*private var defaultTextColor
    private var loadingTextColor
    private var loadingText*/
    private val paintBackgroundNormal = Paint().apply {

    }
    private val textPaint = Paint().apply {
        textSize = context.resources.getDimension(R.dimen.dimen_24sp)
        textAlign = Paint.Align.CENTER
    }

    private var textButton = "Download"
    private val valueAnimator = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { p, old, new ->

    }


    init {

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {

        }
        val styleAttr = context.obtainStyledAttributes(attrs, R.styleable.LoadingButton, 0, 0)

        defaultBackgroundColor = styleAttr.getColor(
            R.styleable.LoadingButton_default_background_color,
            Color.parseColor(resources.getString(R.color.colorPrimary))
        )
        loadingBackgroundColor = styleAttr.getColor(
            R.styleable.LoadingButton_loading_background_color,
            Color.parseColor(resources.getString(R.color.colorPrimaryDark))
        )

        styleAttr.recycle()

    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRectangle(paintBackgroundNormal)
        canvas.drawText(textButton, textPaint)
    }

    private fun Canvas.drawRectangle(paint: Paint) {
        val rectF = RectF(0F, 0F, widthSize.toFloat(), heightSize.toFloat())
        paint.color = defaultBackgroundColor
        this.drawRect(rectF, paint)
    }

    private fun Canvas.drawText(value: String, paint: Paint) {
        paint.color = Color.WHITE
        this.drawText(value, widthSize / 2F, (heightSize - paint.ascent()) / 2F, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}