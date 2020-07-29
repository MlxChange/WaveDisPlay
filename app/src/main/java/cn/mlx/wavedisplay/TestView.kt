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
import androidx.core.graphics.withSave
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

/**
 * Project:WaveDisPlay
 * Created by MLX on 2020/7/28.
 */
class TestView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var path = Path()
    var paint = Paint()
    var drawArrow = true
    var mwidth = 0
    var mheight = 0
    var currentX = 170f
        set(value) {
            field = value
            invalidate()
        }
    var currentY = 1200f
    var angle = 1f

    var huitanX = 0f
        set(value) {
            field = value
            invalidate()
        }

    var showStart = true

    var bianyuanX = 1f
        set(value) {
            field = value
            invalidate()
        }
    private var bianyuanAniamator: ObjectAnimator? = null

    private var animator2: ObjectAnimator? = null
    private var animator = ObjectAnimator.ofFloat(this, "currentX", 1600f)
    private var animator3: ObjectAnimator? = null
    private var animator4: ObjectAnimator? = null

    init {
        animator.interpolator = LinearInterpolator()
        animator.duration = 500



        paint.color = Color.RED
        paint.isAntiAlias = true
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = 10f
        var key0 = Keyframe.ofFloat(0f, 0f)
        var key1 = Keyframe.ofFloat(0.5f, 300f)
        var key2 = Keyframe.ofFloat(0.7f, 150f)
        var key3 = Keyframe.ofFloat(1f, 0f)
        var holder = PropertyValuesHolder.ofKeyframe("huitanX", key0, key1, key2, key3)
        animator3 = ObjectAnimator.ofPropertyValuesHolder(this, holder)
        animator3?.duration = 1000
        animator3?.interpolator = BounceInterpolator()

        animator3?.doOnEnd {
            currentX = 200f
            showStart = true
            currentY = 1200f
            bianyuanX = 1f
            huitanX = 0f
            drawArrow = true
            invalidate()
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var time = measureTimeMillis {
            if (showStart) {
                canvas.save()
                angle = (currentX - 30) / 140 * 0.5f
                var angle2 = Math.pow(currentX.toDouble(), (1.0f / 3).toDouble())
                var angle3 = sqrt(currentX)
                var angle4 = (currentX - 30) / 140 * 0.5f
                angle = max(angle, 0.5f)
                var pathEffect = CornerPathEffect(200f * angle)


                paint.pathEffect = pathEffect

                path.moveTo((60 * angle4 * bianyuanX), 0f)
                canvas.drawPath(path, paint)
                paint.strokeWidth = 10f
                paint.color = Color.RED
                var length = (currentX) / 1.7f
                length = max(length, (170) / 1.7f)
                var shangY = currentY - length
                path.lineTo((60 * angle4 * bianyuanX).toFloat(), shangY)
                path.lineTo(currentX, shangY + length)
                path.lineTo((60 * angle4 * bianyuanX).toFloat(), shangY + length * 2)
                path.lineTo((60 * angle4 * bianyuanX).toFloat(), mheight.toFloat())
                canvas.drawPath(path, paint)
                canvas.restore()


                canvas.save()
                paint.pathEffect = CornerPathEffect(0f)
                var path2 = Path()
                path2.addRect(
                    0f,
                    0f,
                    (60 * angle4 * bianyuanX),
                    mheight.toFloat(),
                    Path.Direction.CCW
                )
                canvas.drawPath(path2, paint)
                path.reset()

                var paint2 = Paint()
                paint2.setColor(Color.WHITE)
                paint2.strokeWidth = 5f
                paint2.isAntiAlias = true
                paint2.style = Paint.Style.STROKE
                paint2.strokeJoin = Paint.Join.ROUND
                paint2.strokeCap = Paint.Cap.ROUND
                var heardPath = Path()
                heardPath.addCircle(
                    (currentX - (17f * angle2) - angle * angle3).toFloat(),
                    currentY,
                    40f,
                    Path.Direction.CCW
                )
                paint2.pathEffect = CornerPathEffect(30f)
                heardPath.moveTo(
                    ((currentX - (17f * angle2) - angle * angle3) - 5f).toFloat(),
                    currentY - 18f
                )
                heardPath.lineTo(
                    ((currentX - (17f * angle2) - angle * angle3) + 15f).toFloat(),
                    currentY
                )
                heardPath.lineTo(
                    ((currentX - (17f * angle2) - angle * angle3) - 5f).toFloat(),
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
                    (mwidth - huitanX).toFloat(),
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
                    animator.addUpdateListener {
                        var angle2 = Math.pow(currentX.toDouble(), (1.0f / 3).toDouble())
                        var angle3 = sqrt(currentX)
                        if ((currentX - (20f * angle2) - angle * angle3).toInt() >= mwidth - 350) {

                            animator.cancel()
                            currentX - 300
                            animator2?.start()
                        }
                    }
                    animator.start()
                } else {
                    Log.i("zzz", "currentX:$currentX")
                    animator4 = ObjectAnimator.ofFloat(this, "currentX", currentX, 170f)
                    animator4?.duration = 800
                    animator4?.interpolator = OvershootInterpolator(3.2f)
                    animator4?.start()
                }
            }

            MotionEvent.ACTION_DOWN -> {
                if (!showStart) {
                    currentY = event.y
                    animator3?.start()
                } else {
                }
            }

        }

        return true
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mwidth = w
        mheight = h

        animator2 = ObjectAnimator.ofFloat(this, "bianyuanX", 5.2f)
        animator2?.duration = 300
        animator2?.interpolator = LinearInterpolator()
        animator2?.doOnEnd {
            showStart = false
            animator3?.start()
        }
    }

}