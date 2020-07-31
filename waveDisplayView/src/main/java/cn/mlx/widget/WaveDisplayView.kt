package cn.mlx.widget

import android.animation.ValueAnimator
import android.content.Context
import android.database.Observable
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.children
import java.lang.IllegalArgumentException
import kotlin.math.abs
import kotlin.math.max


/**
 * Project:NetEasy
 * Created by mlxCh on 2020/7/28.
 */
class WaveDisplayView<T> @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {


    var currentX = 0f
    var currentY = 0f

    var viewList = mutableListOf<ViewHolder>()
    var recyclePool = mutableListOf<ViewHolder>()
    private var mAdapter: WaveAdapter<T>? = null

    private val mObserver: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            refreshView()
        }
    }


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
    private val dragGenerateAnimator = ValueAnimator.ofFloat(0f, 1f)

    var dragToLeftOffset = 0f
    var moveFringeOffset = 0f

    var touchToLeftOffset = 0f
    var fringeToLeftLength = 0f

    var reboundLength = 0f
    var dragReboundX = 0f

    var touchDragButton = false
    var canTouchDrag = true

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
            fringeOffset = 30f
            currentX = mwidth
            drawArrow = true
            dragToLeftOffset = 0f
            moveFringeOffset = 0f
            touchToLeftOffset = 0f
            fringeToLeftLength = 0f

            removeViewAt(childCount - 1)
            this.postDelayed({ dragGenerateAnimator.start() }, 300)
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



        dragGenerateAnimator.duration = 1000
        dragGenerateAnimator.doOnStart {
            canTouchDrag = false
        }
        dragGenerateAnimator.interpolator = OvershootInterpolator()
        dragGenerateAnimator.addUpdateListener {
            currentX = mwidth - 140f * it.animatedValue as Float
            invalidate()
        }
        dragGenerateAnimator.doOnEnd {
            canTouchDrag = true
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var maxwidth = 0
        var maxHeight = 0
        val widthUsed = 0
        val heightUsed = 0

        children.forEachIndexed { index, child ->
            measureChildWithMargins(
                child,
                widthMeasureSpec,
                widthUsed,
                heightMeasureSpec,
                heightUsed
            )
            maxwidth = max(maxwidth, child.measuredWidth)
            maxHeight = max(maxHeight, child.measuredHeight)
        }
        setMeasuredDimension(maxwidth, maxHeight)
    }


    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            touchDragButton =
                !(currentX - event.x > 30 || abs(event.y - currentY) > 50 || !canTouchDrag)
            return touchDragButton
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {

                if (touchDragButton) {
                    currentX = max(mwidth / 3, event.x)

                    currentY = when {
                        event.y > mheight - 80 -> {
                            mheight - 80
                        }
                        event.y < 80 -> {
                            80f
                        }
                        else -> {
                            event.y
                        }
                    }
                    drawArrow = currentX >= mwidth / 2
                    invalidate()
                }
            }
            MotionEvent.ACTION_DOWN -> {
                touchDragButton =
                    !(currentX - event.x > 30 || abs(event.y - currentY) > 50 || !canTouchDrag)
                return touchDragButton
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
        return false
    }


    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        if (childCount > 1) {
            if (child == getChildAt(childCount - 1)) {
                clipCanvas(canvas)
                return super.drawChild(canvas, child, drawingTime)
            }
            if (child == getChildAt(childCount - 2)) {
                return super.drawChild(canvas, child, drawingTime)
            }
        } else if (childCount == 1) {
            return super.drawChild(canvas, child, drawingTime)
        }
        return false
    }

    private fun clipCanvas(canvas: Canvas) {
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
        canvas.clipPath(dragPath, Region.Op.DIFFERENCE)
        dragPath.reset()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mwidth = w.toFloat()
        mheight = h.toFloat()
        currentX = mwidth - 140f
        currentY = mheight / 2
    }

    private fun refreshView() {
        removeAllViews()
        viewList.clear()
        recyclePool.clear()
        mAdapter?.let { adapter ->
            for (i in 0 until adapter.getItemCount()) {
                val viewHolder: ViewHolder? = createViewHolder(i)
                    ?: throw IllegalArgumentException("createViewHolder must not be null")
                addView(viewHolder!!.itemView, 0)
                requestLayout()
            }
        }

    }

    private fun createViewHolder(i: Int): ViewHolder? {
        var viewHolder: ViewHolder? = null
        mAdapter?.let { adapter ->
            if (recyclePool.size <= i) {
                viewHolder = adapter.createViewHolder(this, i)
                viewHolder?.viewType = adapter.getItemViewType(i)
                viewHolder?.position = i
                recyclePool.add(viewHolder!!)
                adapter.bindViewHolder(viewHolder!!, i)
                return viewHolder
            } else {
                viewHolder = recyclePool[i]
                viewHolder?.viewType = adapter.getItemViewType(i)
                viewHolder?.position = i
                adapter.bindViewHolder(viewHolder!!, i)
            }
        }
        return viewHolder
    }

    fun setAdapter(adapter: WaveAdapter<T>) {
        mAdapter = adapter
        mAdapter?.registerObserver(mObserver)
        mAdapter?.notifyDataSetChanged()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            getChildAt(i).layout(l, t, getChildAt(i).measuredWidth, getChildAt(i).measuredHeight)
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    abstract class Adapter<VH : ViewHolder> {

        private val mObservable: AdapterDataObservable = AdapterDataObservable()

        abstract fun createViewHolder(parent: ViewGroup, type: Int): ViewHolder

        abstract fun bindViewHolder(holder: ViewHolder, position: Int)

        abstract fun getItemCount(): Int

        abstract fun getItemViewType(i: Int): Int


        fun notifyDataSetChanged() {
            mObservable.notifyChanged()
        }

        fun registerObserver(observer: AdapterDataObserver?) {
            mObservable.registerObserver(observer)
        }
    }

    class ViewHolder(var itemView: View) {
        var position: Int = -1
        var viewType: Int = -1
        fun getContext(): Context {
            return itemView.context
        }
    }

    class AdapterDataObservable : Observable<AdapterDataObserver>() {

        fun hasObservers(): Boolean {
            return mObservers.isNotEmpty()
        }

        fun notifyChanged() {
            for (i in mObservers.size - 1 downTo 0) {
                mObservers[i].onChanged()
            }
        }
    }

    abstract class AdapterDataObserver {
        open fun onChanged() {}
    }


}