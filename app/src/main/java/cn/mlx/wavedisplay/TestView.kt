package cn.mlx.wavedisplay

import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
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

    private var contentPath = Path()
    private var mainPaint = Paint()
    var drawArrow = true
    var mwidth = 0
    var mheight = 0
    var currentX = 170f
        set(value) {
            field = value
            invalidate()
        }
    var currentY = 1200f

    var cornerAngle = 1f

    var angle2 = 0.0
    var angle3 = 0f
    var angle4 = 0f
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
    private var touchMoveAnimatorX = ObjectAnimator.ofFloat(this, "currentX", 1600f)
    private var reboundAnimator: ObjectAnimator? = null
    private var dragAnimator: ObjectAnimator? = null

    init {
        touchMoveAnimatorX.interpolator = LinearInterpolator()
        touchMoveAnimatorX.duration = 500

        mainPaint.color = Color.RED
        mainPaint.isAntiAlias = true
        mainPaint.strokeCap = Paint.Cap.ROUND
        mainPaint.strokeJoin = Paint.Join.ROUND
        mainPaint.style = Paint.Style.FILL_AND_STROKE
        mainPaint.strokeWidth = 10f

        var key0 = Keyframe.ofFloat(0f, 0f)
        var key1 = Keyframe.ofFloat(0.5f, 300f)
        var key2 = Keyframe.ofFloat(0.7f, 150f)
        var key3 = Keyframe.ofFloat(1f, 0f)
        var holder = PropertyValuesHolder.ofKeyframe("huitanX", key0, key1, key2, key3)

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
                angle2 = Math.pow(currentX.toDouble(), (1.0f / 3).toDouble())
                angle3 = sqrt(currentX)
                angle4 = (currentX - 30) / 140 * 0.5f
                cornerAngle = max(cornerAngle, 0.5f)
                pathEffect = CornerPathEffect(200f * cornerAngle)

                mainPaint.pathEffect = pathEffect

                contentPath.moveTo((60 * angle4 * fringeOffsetSpeed), 0f)
                canvas.drawPath(contentPath, mainPaint)
                mainPaint.strokeWidth = 10f
                mainPaint.color = Color.RED
                var length = (currentX) / 1.7f
                length = max(length, (170) / 1.7f)
                var shangY = currentY - length
                contentPath.lineTo((60 * angle4 * fringeOffsetSpeed), shangY)
                contentPath.lineTo(currentX, shangY + length)
                contentPath.lineTo((60 * angle4 * fringeOffsetSpeed), shangY + length * 2)
                contentPath.lineTo((60 * angle4 * fringeOffsetSpeed), mheight.toFloat())
                canvas.drawPath(contentPath, mainPaint)
                canvas.restore()


                canvas.save()
                mainPaint.pathEffect = CornerPathEffect(0f)
                var path2 = Path()
                path2.addRect(
                    0f,
                    0f,
                    (60 * angle4 * fringeOffsetSpeed),
                    mheight.toFloat(),
                    Path.Direction.CCW
                )
                canvas.drawPath(path2, mainPaint)
                contentPath.reset()

                var paint2 = Paint()
                paint2.setColor(Color.WHITE)
                paint2.strokeWidth = 5f
                paint2.isAntiAlias = true
                paint2.style = Paint.Style.STROKE
                paint2.strokeJoin = Paint.Join.ROUND
                paint2.strokeCap = Paint.Cap.ROUND
                var heardPath = Path()
                heardPath.addCircle(
                    (currentX - (17f * angle2) - cornerAngle * angle3).toFloat(),
                    currentY,
                    40f,
                    Path.Direction.CCW
                )
                paint2.pathEffect = CornerPathEffect(30f)
                heardPath.moveTo(
                    ((currentX - (17f * angle2) - cornerAngle * angle3) - 5f).toFloat(),
                    currentY - 18f
                )
                heardPath.lineTo(
                    ((currentX - (17f * angle2) - cornerAngle * angle3) + 15f).toFloat(),
                    currentY
                )
                heardPath.lineTo(
                    ((currentX - (17f * angle2) - cornerAngle * angle3) - 5f).toFloat(),
                    currentY + 18f
                )
                if (currentX > 35f) {
                    if (drawArrow) {
                        canvas.drawPath(heardPath, paint2)
                    }

                }
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
                    drawArrow = false
                    touchMoveAnimatorX.addUpdateListener {
                        var angle2 = Math.pow(currentX.toDouble(), (1.0f / 3).toDouble())
                        var angle3 = sqrt(currentX)
                        if ((currentX - (20f * angle2) - cornerAngle * angle3).toInt() >= mwidth - 350) {

                            touchMoveAnimatorX.cancel()
                            currentX - 300
                            fringeOffsetAnimator?.start()
                        }
                    }
                    touchMoveAnimatorX.start()
                } else {
                    Log.i("zzz", "currentX:$currentX")
                    dragAnimator = ObjectAnimator.ofFloat(this, "currentX", currentX, 170f)
                    dragAnimator?.duration = 800
                    dragAnimator?.interpolator = OvershootInterpolator(3.2f)
                    dragAnimator?.start()
                }
            }

            MotionEvent.ACTION_DOWN -> {
                if (!showStartScreen) {
                    currentY = event.y
                    reboundAnimator?.start()
                }
            }

        }

        return true
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mwidth = w
        mheight = h

        fringeOffsetAnimator = ObjectAnimator.ofFloat(this, "fringeOffsetSpeed", 5.2f)
        fringeOffsetAnimator?.duration = 300
        fringeOffsetAnimator?.interpolator = LinearInterpolator()
        fringeOffsetAnimator?.doOnEnd {
            showStartScreen = false
            reboundAnimator?.start()
        }
    }

}