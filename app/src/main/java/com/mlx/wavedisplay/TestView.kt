package com.mlx.wavedisplay

import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.*
import androidx.core.animation.doOnEnd
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

/**
 * Project:WaveDisPlay
 * Created by MLX on 2020/7/28.
 */
class TestView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var arrowPaint = Paint()
    private var mainPaint = Paint()

    private var contentPath = Path()
    private var coverPath = Path()
    private var arrowPath = Path()

    var drawArrow = true
    var mwidth = 0
    var mheight = 0
    var currentX = 170f
        set(value) {
            field = value
            invalidate()
        }
    var currentY = 1200f
    var protrudingRadius=0f
    var cornerAngle = 1f

    var arrowOffset = 0.0
    var fringeOffset = 0f
    lateinit var pathEffect: PathEffect

    private var reboundOffsetX = 0f
        set(value) {
            field = value
            invalidate()
        }

    private var showStartScreen = true

    private var fringeOffsetSpeed = 1f
        set(value) {
            field = value
            invalidate()
        }


    private var fringeOffsetAnimator: ObjectAnimator? = null
    private var touchMoveAnimatorX : ObjectAnimator?=null
    private var reboundAnimator: ObjectAnimator? = null
    private var dragAnimator: ObjectAnimator? = null

    init {

        mainPaint.color = Color.RED
        mainPaint.isAntiAlias = true
        mainPaint.strokeCap = Paint.Cap.ROUND
        mainPaint.strokeJoin = Paint.Join.ROUND
        mainPaint.style = Paint.Style.FILL_AND_STROKE
        mainPaint.strokeWidth = 10f

        arrowPaint.setColor(Color.WHITE)
        arrowPaint.strokeWidth = 5f
        arrowPaint.isAntiAlias = true
        arrowPaint.style = Paint.Style.STROKE
        arrowPaint.strokeJoin = Paint.Join.ROUND
        arrowPaint.strokeCap = Paint.Cap.ROUND

        val key0 = Keyframe.ofFloat(0f, 0f)
        val key1 = Keyframe.ofFloat(0.5f, 300f)
        val key2 = Keyframe.ofFloat(0.7f, 150f)
        val key3 = Keyframe.ofFloat(1f, 0f)
        val holder = PropertyValuesHolder.ofKeyframe("reboundOffsetX", key0, key1, key2, key3)

        reboundAnimator = ObjectAnimator.ofPropertyValuesHolder(this, holder)
        reboundAnimator?.duration = 1000
        reboundAnimator?.interpolator = BounceInterpolator()

        reboundAnimator?.doOnEnd {
            currentX = 200f
            showStartScreen = true
            currentY = 1200f
            fringeOffsetSpeed = 1f
            reboundOffsetX = 0f
            drawArrow = true
            invalidate()
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var time = measureTimeMillis {
            if (showStartScreen) {
                canvas.save()
                cornerAngle = (currentX - 30) / 140 * 0.5f
                arrowOffset = Math.pow(currentX.toDouble(), (1.0f / 3).toDouble())*17f+sqrt(currentX)*cornerAngle

                fringeOffset = (currentX - 30) / 140 * 30f
                cornerAngle = max(cornerAngle, 0.5f)
                pathEffect = CornerPathEffect(200f * cornerAngle)

                mainPaint.pathEffect = pathEffect

                contentPath.moveTo(( fringeOffset * fringeOffsetSpeed), 0f)
                canvas.drawPath(contentPath, mainPaint)
                mainPaint.strokeWidth = 10f
                mainPaint.color = Color.RED
                protrudingRadius = (currentX) / 1.7f
                protrudingRadius = max(protrudingRadius, (170) / 1.7f)

                contentPath.lineTo(( fringeOffset * fringeOffsetSpeed), currentY - protrudingRadius)
                contentPath.lineTo(currentX, currentY - protrudingRadius + protrudingRadius)
                contentPath.lineTo(( fringeOffset * fringeOffsetSpeed), currentY - protrudingRadius + protrudingRadius * 2)
                contentPath.lineTo(( fringeOffset * fringeOffsetSpeed), mheight.toFloat())

                canvas.drawPath(contentPath, mainPaint)
                canvas.restore()


                canvas.save()
                mainPaint.pathEffect = CornerPathEffect(0f)

                coverPath.addRect(
                    0f,
                    0f,
                    ( fringeOffset * fringeOffsetSpeed),
                    mheight.toFloat(),
                    Path.Direction.CCW
                )
                canvas.drawPath(coverPath, mainPaint)
                coverPath.reset()
                contentPath.reset()




                arrowPath.addCircle(
                    (currentX - arrowOffset).toFloat(),
                    currentY,
                    40f,
                    Path.Direction.CCW
                )
                arrowPaint.pathEffect = CornerPathEffect(30f)
                arrowPath.moveTo(
                    ((currentX -  arrowOffset) - 5f).toFloat(),
                    currentY - 18f
                )
                arrowPath.lineTo(
                    ((currentX -  arrowOffset ) + 15f).toFloat(),
                    currentY
                )
                arrowPath.lineTo(
                    ((currentX - arrowOffset) - 5f).toFloat(),
                    currentY + 18f
                )
                if (currentX > 35f) {
                    if (drawArrow) {
                        canvas.drawPath(arrowPath, arrowPaint)
                    }

                }
                arrowPath.reset()
                canvas.restore()
            } else {
                canvas.save()
                var endPath = Path()
                endPath.lineTo(mwidth.toFloat(), 0f)
                endPath.quadTo(
                    (mwidth - reboundOffsetX).toFloat(),
                    currentY,
                    mwidth.toFloat(),
                    mheight.toFloat()
                )
                endPath.lineTo(0f, mheight.toFloat())
                var paint5 = Paint()
                paint5.style = Paint.Style.FILL
                paint5.color = Color.RED
                paint5.isAntiAlias = true
                canvas.drawPath(endPath, paint5)
            }

        }

    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                currentX = event.x + 120f
                currentY = event.y
                drawArrow = currentX - 120f <= mwidth / 2
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                if (currentX - 120f > mwidth / 2) {
                    touchMoveAnimatorX= ObjectAnimator.ofFloat(this,"currentX",1600f)
                    touchMoveAnimatorX?.interpolator = LinearInterpolator()
                    touchMoveAnimatorX?.duration = 500
                    drawArrow = false
                    fringeOffsetAnimator = ObjectAnimator.ofFloat(this, "fringeOffsetSpeed", 1.1f,5.2f)
                    fringeOffsetAnimator?.duration = 300
                    fringeOffsetAnimator?.interpolator = LinearInterpolator()
                    fringeOffsetAnimator?.doOnEnd {
                        showStartScreen = false
                        reboundAnimator?.start()
                    }
                    touchMoveAnimatorX?.addUpdateListener {
                        var angle2 = Math.pow(currentX.toDouble(), (1.0f / 3).toDouble())
                        var angle3 = sqrt(currentX)
                        if ((currentX - (20f * angle2) - cornerAngle * angle3).toInt() >= mwidth - 350) {

                            touchMoveAnimatorX?.cancel()
                            currentX - 300
                            fringeOffsetAnimator?.start()
                        }
                    }
                    touchMoveAnimatorX?.start()
                } else {
                    dragAnimator = ObjectAnimator.ofFloat(this, "currentX", currentX, 170f)
                    dragAnimator?.duration = 800
                    dragAnimator?.interpolator = OvershootInterpolator(3.2f)
                    dragAnimator?.start()
                }
            }
        }
        return true
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mwidth = w
        mheight = h
    }

}