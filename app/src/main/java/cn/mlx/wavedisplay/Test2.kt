package cn.mlx.wavedisplay

import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.graphics.withSave
import kotlin.math.max
import kotlin.math.min

/**
 * Project:WaveDisPlay
 * Created by mlxCh on 2020/7/30.
 */
class Test2 @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    var currentX = 0f
    var currentY = 0f

    private var arrowPath = Path()

    var mwidth = 0f
    var mheight = 0f

    var drawArrow = true

    private var arrowPaint = Paint()

    var fringeOffset = 30f

    private var touchMoveAnimatorX = ValueAnimator.ofFloat(0f, 1f)
    private var dragAnimator = ValueAnimator.ofFloat(0f, 1f)

    var touchOffset = 0f
    var moveFringeOffset = 0f

    var touchLength = 0f
    var touchFrineg = 0f

    var currentDrag = 0f
    var dragX = 0f

    private var fringeOffsetSpeed = 1f
        set(value) {
            field = value
            invalidate()
        }


    init {
        arrowPaint.setColor(Color.WHITE)
        arrowPaint.strokeWidth = 5f
        arrowPaint.isAntiAlias = true
        arrowPaint.style = Paint.Style.STROKE
        arrowPaint.strokeJoin = Paint.Join.ROUND
        arrowPaint.strokeCap = Paint.Cap.ROUND

        touchMoveAnimatorX.duration = 1200
        touchMoveAnimatorX.interpolator = BounceInterpolator()
        touchMoveAnimatorX.doOnStart {
            touchLength = currentX
            touchFrineg = mwidth - fringeOffset
        }
        touchMoveAnimatorX.addUpdateListener {
            moveFringeOffset = it.animatedValue as Float * touchFrineg
            touchOffset = it.animatedValue as Float * touchLength
            invalidate()
        }
        touchMoveAnimatorX.doOnEnd {
            currentX = mwidth - 120f
            currentY = 1200f
            fringeOffset = 30f
            drawArrow = true
            touchOffset = 0f
            moveFringeOffset = 0f
            touchLength = 0f
            touchFrineg = 0f
        }

        dragAnimator.doOnStart {
            currentDrag = mwidth - currentX - 120
            dragX = currentX
        }
        dragAnimator.duration = 700
        dragAnimator.interpolator = OvershootInterpolator(3f)
        dragAnimator.addUpdateListener {
            currentX = dragX + it.animatedValue as Float * currentDrag
            invalidate()
        }

    }

    override fun onDraw(canvas: Canvas) {

        canvas.save()
        //currentX = max(mwidth / 4, currentX)
        var touchWidth = mwidth - 30f - currentX
        var touchHeight = touchWidth / 0.7f
        if (touchWidth < 90) {
            touchHeight /= (touchWidth / 90)
        }
        fringeOffset = (mwidth - currentX - 30) / 120 * 30f + moveFringeOffset
        var fitstValuePointf = PointF(mwidth - fringeOffset, currentY - touchHeight)
        var secondValuePointf = PointF(currentX - touchOffset, currentY)
        var valuePointF3 = PointF(mwidth - fringeOffset, currentY + touchHeight)

        var topcon1 =
            PointF(fitstValuePointf.x, (fitstValuePointf.y + secondValuePointf.y) / 2 + 30f)
        var topcon2 = PointF(
            secondValuePointf.x + (mwidth - secondValuePointf.x - fringeOffset) * 0.06f,
            (fitstValuePointf.y + secondValuePointf.y) / 2
        )

        var bottom1 = PointF(
            secondValuePointf.x + (mwidth - secondValuePointf.x - fringeOffset) * 0.06f,
            (secondValuePointf.y + valuePointF3.y) / 2
        )
        var bottom2 = PointF(
            valuePointF3.x,
            secondValuePointf.y + (secondValuePointf.y - fitstValuePointf.y) / 2 - 30f
        )

        var path = Path()
        path.moveTo(mwidth, 0f)
        path.lineTo(mwidth - fringeOffset, 0f)
        path.lineTo(mwidth - fringeOffset, currentY - touchHeight)
        path.lineTo(fitstValuePointf.x, fitstValuePointf.y)
        path.cubicTo(
            topcon1.x,
            topcon1.y,
            topcon2.x,
            topcon2.y,
            secondValuePointf.x,
            secondValuePointf.y
        )
        path.cubicTo(bottom1.x, bottom1.y, bottom2.x, bottom2.y, valuePointF3.x, valuePointF3.y)
        path.lineTo(mwidth - fringeOffset, mheight)
        path.lineTo(mwidth, mheight)
        var paint = Paint()
        paint.color = Color.RED
        paint.style = Paint.Style.FILL_AND_STROKE
        canvas.drawPath(path, paint)
        paint.color = Color.BLACK
        canvas.drawCircle(fitstValuePointf.x, fitstValuePointf.y, 5f, paint)
        canvas.drawCircle(secondValuePointf.x, secondValuePointf.y, 5f, paint)
        canvas.drawCircle(valuePointF3.x, valuePointF3.y, 5f, paint)

        paint.color = Color.BLUE
        canvas.drawCircle(topcon1.x, topcon1.y, 5f, paint)
        paint.color = Color.GREEN
        canvas.drawCircle(topcon2.x, topcon2.y, 5f, paint)
        canvas.drawCircle(bottom1.x, bottom1.y, 5f, paint)
        paint.color = Color.BLUE
        canvas.drawCircle(bottom2.x, bottom2.y, 5f, paint)
        canvas.restore()

        canvas.save()
        if (drawArrow) {
            arrowPath.addCircle(
                currentX + 50f,
                currentY,
                30f,
                Path.Direction.CCW
            )
            arrowPaint.pathEffect = CornerPathEffect(30f)
            arrowPath.moveTo(
                (currentX + 55f),
                currentY - 15f
            )
            arrowPath.lineTo(
                (currentX + 35f),
                currentY
            )
            arrowPath.lineTo(
                (currentX + 55f),
                currentY + 15f
            )
            canvas.drawPath(arrowPath, arrowPaint)
        }
        arrowPath.reset()
        canvas.restore()

    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mwidth = w.toFloat()
        mheight = h.toFloat()
        currentX = mwidth - 120f
        currentY = 1200f
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                currentX = max(mwidth / 3, event.x)
                currentY = event.y
                drawArrow = currentX >= mwidth / 2
                invalidate()
            }
            MotionEvent.ACTION_DOWN -> {
            }
            MotionEvent.ACTION_UP -> {
                if (currentX < mwidth / 2) {
                    drawArrow = false
                    touchMoveAnimatorX.start()
                } else {
                    dragAnimator.start()
                }
            }
        }
        return true

    }


}