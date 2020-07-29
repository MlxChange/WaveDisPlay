package cn.mlx.wavedisplay

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.graphics.withSave
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

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
    var currentX = 240f
        set(value) {
            field = value
            invalidate()
        }
    var currentY = 1200f
    var angle = 1f

    var bianyuanX = 1f
        set(value) {
            field = value
            invalidate()
        }
    private var bianyuanAniamator: ObjectAnimator? = null

    private var animator2: ObjectAnimator? = null
    private var animator = ObjectAnimator.ofFloat(this, "currentX", 1600f)

    init {
        animator.interpolator = LinearInterpolator()
        animator.duration = 1000



        paint.color = Color.RED
        paint.isAntiAlias = true
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = 10f

    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        angle = (currentX - 100) / 140 * 0.5f
        var angle2 = Math.pow(currentX.toDouble(), (1.0f / 3).toDouble())
        var angle3 = sqrt(currentX)
        var angle4 = (currentX - 100) / 140 * 0.5f
        angle = max(angle, 0.5f)
        var pathEffect = CornerPathEffect(200f * angle)


        paint.pathEffect = pathEffect

        path.moveTo((60 * angle4 * bianyuanX), 0f)
        canvas.drawPath(path, paint)
        paint.strokeWidth = 10f
        paint.color = Color.RED
        var length = (currentX - 50) / 2f
        length = max(length, (280 - 50) / 2f)
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
        path2.addRect(0f, 0f, (60 * angle4 * bianyuanX), mheight.toFloat(), Path.Direction.CCW)
        canvas.drawPath(path2, paint)
        path.reset()

        var paint2 = Paint()
        paint2.setColor(Color.BLUE)
        paint2.strokeWidth = 5f
        paint2.isAntiAlias = true
        paint2.style = Paint.Style.STROKE
        paint2.strokeJoin = Paint.Join.ROUND
        paint2.strokeCap = Paint.Cap.ROUND
        var heardPath = Path()
        heardPath.addCircle(
            (currentX - (20f * angle2) - angle * angle3).toFloat(),
            currentY,
            45f,
            Path.Direction.CCW
        )
        paint2.pathEffect = CornerPathEffect(30f)
        heardPath.moveTo(
            ((currentX - (20f * angle2) - angle * angle3) - 5f).toFloat(),
            currentY - 22.5f
        )
        heardPath.lineTo(((currentX - (20f * angle2) - angle * angle3) + 20f).toFloat(), currentY)
        heardPath.lineTo(
            ((currentX - (20f * angle2) - angle * angle3) - 5f).toFloat(),
            currentY + 22.5f
        )
        if (currentX > 35f) {
            if (drawArrow) {
                canvas.drawPath(heardPath, paint2)
            }

        }
        canvas.restore()
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                currentX = event.x + 120f
                currentY = event.y
                drawArrow = true
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                if (currentX - 120f > mwidth / 2) {
                    drawArrow = false
                    animator.addUpdateListener {
                        var angle2 = Math.pow(currentX.toDouble(), (1.0f / 3).toDouble())
                        var angle3 = sqrt(currentX)
                        if ((currentX - (20f * angle2) - angle * angle3).toInt() >= mwidth - 350) {
                            Log.i("zzz", "currentX:$currentX")
                            animator.cancel()
                            animator2?.start()
                        }
                    }
                    animator.start()

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
        animator2?.duration = 500
        animator2?.interpolator = DecelerateInterpolator()
    }

}