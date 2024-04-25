package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import com.udacity.utils.Logger
import kotlin.math.min
import kotlin.properties.Delegates

@SuppressLint("ResourceType")
class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var progressValue = 0.0F

    private var defaultBackgroundColor = Color.parseColor(resources.getString(R.color.colorPrimary))
    private var loadingBackgroundColor =
        Color.parseColor(resources.getString(R.color.colorPrimaryDark))
    private var defaultText = "Download"
    private var loadingText = "Loading"
    private var buttonText = "Download"

    /*private var defaultTextColor
    private var loadingTextColor*/
    private var btnTextBound: Rect = Rect()
    private val paintBackgroundNormal = Paint().apply {
        strokeCap = Paint.Cap.ROUND
    }
    private val paintProgress = Paint().apply {
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint().apply {
        textSize = context.resources.getDimension(R.dimen.dimen_24sp)
        textAlign = Paint.Align.CENTER
    }
    private val paintArcProgress = Paint().apply {
        isAntiAlias = true
    }

    //For Arc shape Progress
    private val progressOvalRect = RectF()
    private var currentProgressArc = 0f
    private var progressArcSize = 0f
    private val progressArcAnimator = ValueAnimator.ofFloat(0f, 360F).apply {
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            currentProgressArc = it.animatedValue as Float
            invalidate()
        }
    }
    private var valueAnimator = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { _, _, new ->
        when (new) {
            ButtonState.Clicked -> {
                buttonText = defaultText
            }

            ButtonState.Completed -> {
                valueAnimator.cancel()
                progressArcAnimator.cancel()
                currentProgressArc = 0F
                valueAnimator = ValueAnimator.ofFloat(progressValue, 1F).apply {
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            buttonText = defaultText
                            progressValue = 0F
                            invalidate()
                        }
                    })
                    start()
                }
            }

            ButtonState.Loading -> {
                btnTextBound = Rect()
                textPaint.getTextBounds(
                    buttonText,
                    0,
                    buttonText.length,
                    btnTextBound
                )

                buttonText = loadingText
                valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
                    addUpdateListener {
                        Logger.d("progressValue --- $progressValue")
                        progressValue = it.animatedValue as Float
                        invalidate()
                    }
                    start()
                }
                progressArcAnimator.start()
            }
        }
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
        defaultText = styleAttr.getString(R.styleable.LoadingButton_default_text) ?: defaultText
        loadingText = styleAttr.getString(R.styleable.LoadingButton_loading_text) ?: loadingText

        styleAttr.recycle()

    }

    fun changeStateOfButton(state: ButtonState) {
        if (state != this.buttonState) {
            this.buttonState = state
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRectangle(paintBackgroundNormal)
        canvas.drawProgressRectangle(paintProgress, progressValue)
        canvas.drawArcProgress(paintArcProgress)
        canvas.drawText(buttonText, textPaint)
    }

    private fun Canvas.drawRectangle(paint: Paint) {
        val rectF = RectF(0F, 0F, widthSize.toFloat(), heightSize.toFloat())
        paint.color = defaultBackgroundColor
        //this.drawRect(rectF, paint)
        this.drawRoundRect(rectF, 40f, 40f, paint)
    }

    private fun Canvas.drawArcProgress(paint: Paint) {
        val horizontalCenter =
            (btnTextBound.right + btnTextBound.width() + 16F)
        val verticalCenter = heightSize.div(2)

        progressOvalRect.set(
            horizontalCenter - progressArcSize,
            verticalCenter - progressArcSize,
            horizontalCenter + progressArcSize,
            verticalCenter + progressArcSize
        )
        paint.color = Color.parseColor(resources.getString(R.color.colorAccent))
        this.drawArc(progressOvalRect, 0F, currentProgressArc, true, paint)
    }

    private fun Canvas.drawProgressRectangle(paint: Paint, progress: Float) {
        val rectF = RectF(0F, 0F, widthSize.toFloat() * progress, heightSize.toFloat())
        paint.color = loadingBackgroundColor
        //this.drawRect(rectF, paint)
        this.drawRoundRect(rectF, 40f, 40f, paint)
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        progressArcSize = (min(w, h).div(2)).times(0.4F)
    }

}