package com.mlx.wavedisplay

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import kotlin.math.abs
import kotlin.math.max

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
    private var dragPath = Path()

    var mwidth = 0f
    var mheight = 0f

    var drawArrow = true

    private var arrowPaint = Paint()
    private var dragPaint = Paint()

    private val dragTopValuePoint = PointF(0f, 0f)
    private val dragPeakValuePoint = PointF(0f, 0f)
    private val dragBottomValuePoint = PointF(0f, 0f)

    private val dragTopControlPoint1 = PointF(0f, 0f)
    private val dragTopControlPoint2 = PointF(0f, 0f)
    private val dragBottomControlPoint1 = PointF(0f, 0f)
    private val dragBottomControlPoint2 = PointF(0f, 0f)

    var dragButtonWidth = 0f
    var dragButtonHeight = 0f
    var fringeOffset = 40f

    private val touchMoveAnimator = ValueAnimator.ofFloat(0f, 1f)
    private val dragReboundAnimator = ValueAnimator.ofFloat(0f, 1f)

    var dragToLeftOffset = 0f
    var moveFringeOffset = 0f

    var touchToLeftOffset = 0f
    var fringeToLeftLength = 0f

    var reboundLength = 0f
    var dragReboundX = 0f

    var touchDragButton = false


    init {

        dragPaint.color = Color.RED
        dragPaint.isAntiAlias = true
        dragPaint.style = Paint.Style.FILL_AND_STROKE

        arrowPaint.color = Color.WHITE
        arrowPaint.strokeWidth = 5f
        arrowPaint.pathEffect = CornerPathEffect(30f)
        arrowPaint.isAntiAlias = true
        arrowPaint.style = Paint.Style.STROKE
        arrowPaint.strokeJoin = Paint.Join.ROUND
        arrowPaint.strokeCap = Paint.Cap.ROUND

        touchMoveAnimator.duration = 1200
        touchMoveAnimator.interpolator = BounceInterpolator()
        touchMoveAnimator.doOnStart {
            touchToLeftOffset = currentX
            fringeToLeftLength = mwidth - fringeOffset
        }
        touchMoveAnimator.addUpdateListener {
            moveFringeOffset = it.animatedValue as Float * fringeToLeftLength
            dragToLeftOffset = it.animatedValue as Float * touchToLeftOffset
            invalidate()
        }
        touchMoveAnimator.doOnEnd {
            currentX = mwidth - 140f
            currentY = 1200f
            fringeOffset = 30f
            drawArrow = true
            dragToLeftOffset = 0f
            moveFringeOffset = 0f
            touchToLeftOffset = 0f
            fringeToLeftLength = 0f
        }

        dragReboundAnimator.doOnStart {
            reboundLength = mwidth - currentX - 140
            dragReboundX = currentX
        }
        dragReboundAnimator.duration = 700
        dragReboundAnimator.interpolator = OvershootInterpolator(3f)
        dragReboundAnimator.addUpdateListener {
            currentX = dragReboundX + it.animatedValue as Float * reboundLength
            invalidate()
        }

    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        dragButtonWidth = mwidth - 30f - currentX
        dragButtonHeight = dragButtonWidth / 0.7f
        if (dragButtonWidth < 90) {
            dragButtonHeight /= (dragButtonWidth / 90)
        }
        fringeOffset = (mwidth - currentX - 30) / 120 * 30f + moveFringeOffset
        dragTopValuePoint.x = mwidth - fringeOffset
        dragTopValuePoint.y = currentY - dragButtonHeight
        dragPeakValuePoint.x = currentX - dragToLeftOffset
        dragPeakValuePoint.y = currentY
        dragBottomValuePoint.x = mwidth - fringeOffset
        dragBottomValuePoint.y = currentY + dragButtonHeight

        dragTopControlPoint1.x = dragTopValuePoint.x
        dragTopControlPoint1.y = (dragTopValuePoint.y + dragPeakValuePoint.y) / 2 + 30f

        dragTopControlPoint2.x =
            dragPeakValuePoint.x + (mwidth - dragPeakValuePoint.x - fringeOffset) * 0.06f
        dragTopControlPoint2.y = (dragTopValuePoint.y + dragPeakValuePoint.y) / 2

        dragBottomControlPoint1.x =
            dragPeakValuePoint.x + (mwidth - dragPeakValuePoint.x - fringeOffset) * 0.06f
        dragBottomControlPoint1.y = (dragPeakValuePoint.y + dragBottomValuePoint.y) / 2


        dragBottomControlPoint2.x = dragBottomValuePoint.x
        dragBottomControlPoint2.y =
            dragPeakValuePoint.y + (dragPeakValuePoint.y - dragTopValuePoint.y) / 2 - 30f


        dragPath.moveTo(mwidth, 0f)
        dragPath.lineTo(mwidth - fringeOffset, 0f)
        dragPath.lineTo(mwidth - fringeOffset, currentY - dragButtonHeight)
        dragPath.lineTo(dragTopValuePoint.x, dragTopValuePoint.y)
        dragPath.cubicTo(
            dragTopControlPoint1.x,
            dragTopControlPoint1.y,
            dragTopControlPoint2.x,
            dragTopControlPoint2.y,
            dragPeakValuePoint.x,
            dragPeakValuePoint.y
        )
        dragPath.cubicTo(
            dragBottomControlPoint1.x,
            dragBottomControlPoint1.y,
            dragBottomControlPoint2.x,
            dragBottomControlPoint2.y,
            dragBottomValuePoint.x,
            dragBottomValuePoint.y
        )
        dragPath.lineTo(mwidth - fringeOffset, mheight)
        dragPath.lineTo(mwidth, mheight)
        canvas.drawPath(dragPath, dragPaint)
        dragPath.reset()
        canvas.restore()

        canvas.save()
        if (drawArrow) {
            arrowPath.addCircle(
                currentX + 50f,
                currentY,
                30f,
                Path.Direction.CCW
            )
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
        currentX = mwidth - 140f
        currentY = 1200f
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                if (touchDragButton) {
                    currentX = max(mwidth / 3, event.x)
                    currentY = event.y
                    drawArrow = currentX >= mwidth / 2
                    invalidate()
                }
            }
            MotionEvent.ACTION_DOWN -> {
                touchDragButton = !(currentX - event.x > 30 || abs(event.y - currentY) > 50)
            }
            MotionEvent.ACTION_UP -> {
                if (touchDragButton) {
                    if (currentX < mwidth / 2) {
                        drawArrow = false
                        touchMoveAnimator.start()
                    } else {
                        dragReboundAnimator.start()
                    }
                }
            }
        }
        return true
    }


}