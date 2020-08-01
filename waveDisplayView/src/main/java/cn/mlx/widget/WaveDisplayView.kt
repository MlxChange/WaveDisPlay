package cn.mlx.widget

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.database.Observable
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.children
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


/**
 * Project:NetEasy
 * Created by mlxCh on 2020/7/28.
 */
class WaveDisplayView<T> @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {


    val TAG = "WaveDisplayView"

    val CLIP_RIGHT = 0
    val CLIP_LEFT = 1

    val MAX_SINGLE_CLICK_TIME=50
    val MAX_MOVE_FOR_CLICK=50
    val MAX_LONG_PRESS_TIME=350

    var clipOrientation = CLIP_RIGHT

    var currentX = 0f
    var currentY = 0f

    var arrowRadius=30f

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

    private val dragTopValuePoint = PointF(0f, 0f)
    private val dragPeakValuePoint = PointF(0f, 0f)
    private val dragBottomValuePoint = PointF(0f, 0f)

    private val dragTopControlPoint1 = PointF(0f, 0f)
    private val dragTopControlPoint2 = PointF(0f, 0f)
    private val dragBottomControlPoint1 = PointF(0f, 0f)
    private val dragBottomControlPoint2 = PointF(0f, 0f)

    var dragButtonWidth = 0f
    var dragButtonHeight = 0f
    var fringeOffset = 15f.dp

    var dragWidth=51.5f.dp

    private val touchMoveAnimator = ValueAnimator.ofFloat(0f, 1f)
    private val dragReboundAnimator = ValueAnimator.ofFloat(0f, 1f)
    private val dragGenerateAnimator = ValueAnimator.ofFloat(0f, 1f)

    var dragToLeftOffset = 0f
    var dragToRightOffset = 0f
    var moveFringeOffset = 0f

    var touchToLeftOffset = 0f
    var fringeToLeftLength = 0f

    var touchToRightOffset = 0f
    var fringeToRightLength = 0f

    var reboundLength = 0f
    var dragReboundX = 0f

    var touchDragButton = false
    var canTouchDrag = true

    var currentIndex = -1
    var changeOrientation=false
    private var turnPage = false
    private var dragLocation=0f

    init {



        val a = getContext().obtainStyledAttributes(attrs, R.styleable.WaveDisplayView,
            defStyleAttr, 0)
        val arrowColor=a.getColor(R.styleable.WaveDisplayView_dragColor,Color.parseColor("#ffffff"))
        dragWidth = a.getDimension(R.styleable.WaveDisplayView_dragWidth,51.5f.dp)
        dragLocation = a.getDimension(R.styleable.WaveDisplayView_dragLocation, 0f)

        arrowPaint.color = arrowColor
        arrowPaint.strokeWidth = 5f
        arrowPaint.pathEffect = CornerPathEffect(11.5f.dp)
        arrowPaint.isAntiAlias = true
        arrowPaint.style = Paint.Style.STROKE
        arrowPaint.strokeJoin = Paint.Join.ROUND
        arrowPaint.strokeCap = Paint.Cap.ROUND

        touchMoveAnimator.duration = 1200
        touchMoveAnimator.interpolator = BounceInterpolator()
        touchMoveAnimator.doOnStart {
            if (clipOrientation == CLIP_RIGHT) {
                touchToLeftOffset = currentX
                fringeToLeftLength = mwidth - fringeOffset
            } else {
                touchToRightOffset = mwidth - currentX + 11.5f.dp
                fringeToRightLength = mwidth - fringeOffset
            }
        }
        touchMoveAnimator.addUpdateListener {
            if (clipOrientation == CLIP_RIGHT) {
                moveFringeOffset = it.animatedValue as Float * fringeToLeftLength
                dragToLeftOffset = it.animatedValue as Float * touchToLeftOffset
                invalidate()
            } else {
                moveFringeOffset = it.animatedValue as Float * fringeToRightLength
                dragToRightOffset = it.animatedValue as Float * touchToRightOffset
                invalidate()
            }
        }
        touchMoveAnimator.doOnEnd {
            turnPage=true
            dragGenerateAnimator.start()
        }


        dragReboundAnimator.doOnStart {
            if (clipOrientation == CLIP_RIGHT) {
                reboundLength = mwidth - currentX - dragWidth
                dragReboundX = currentX
            } else {
                reboundLength = currentX - dragWidth
                dragReboundX = currentX
            }

        }
        dragReboundAnimator.duration = 700
        dragReboundAnimator.interpolator = OvershootInterpolator(3f)
        dragReboundAnimator.addUpdateListener {
            currentX = if (clipOrientation == CLIP_RIGHT) {
                dragReboundX + it.animatedValue as Float * reboundLength
            } else {
                dragReboundX - it.animatedValue as Float * reboundLength
            }
            invalidate()
        }



        dragGenerateAnimator.duration = 1000
        dragGenerateAnimator.doOnStart {
            if(!changeOrientation && turnPage){
                addNextOrPreView()
                turnPage=false
            }
            canTouchDrag = false
            currentX = if (clipOrientation == CLIP_RIGHT) {
                if(changeOrientation){
                    var holder=recyclePool[currentIndex+1]
                    if(holder!=null){
                        removeViewAt(childCount-2)
                        addView(holder.itemView,0)
                    }
                    changeOrientation=false
                }
                mwidth
            } else {
                if(changeOrientation){
                    var holder=recyclePool[currentIndex-1]
                    if(holder!=null){
                        removeViewAt(childCount-2)
                        addView(holder.itemView,0)
                    }
                    changeOrientation=false
                }
                0f
            }
            drawArrow = true
            dragToLeftOffset = 0f
            dragToRightOffset = 0f
            touchToRightOffset = 0f
            moveFringeOffset = 0f
            touchToLeftOffset = 0f
            fringeToLeftLength = 0f
        }
        dragGenerateAnimator.interpolator = OvershootInterpolator()
        dragGenerateAnimator.addUpdateListener {
            currentX = if (clipOrientation == CLIP_RIGHT) {
                mwidth - dragWidth * it.animatedValue as Float
            } else {
                dragWidth * it.animatedValue as Float
            }
            invalidate()
        }
        dragGenerateAnimator.doOnEnd {
            canTouchDrag = true
        }


    }

    val Float.dp
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            Resources.getSystem().displayMetrics
        )



    private fun addNextOrPreView() {

        if (clipOrientation == CLIP_RIGHT) {
            if (currentIndex < recyclePool.size - 2) {
                currentIndex += 1
                val lastIndex = currentIndex + 1
                removeViewAt(childCount - 1)
                addView(recyclePool[lastIndex].itemView, 0)
            } else {
                clipOrientation = CLIP_LEFT
                var view = getChildAt(childCount-1)
                removeViewAt(childCount-1)
                currentIndex += 1
                addView(view, 0)
            }
        } else {
            if (currentIndex > 1) {
                currentIndex -= 1
                var perIndex = currentIndex - 1
                addView(recyclePool[perIndex].itemView, 0)
                removeViewAt(childCount - 1)
            } else {
                clipOrientation = CLIP_RIGHT
                var view = getChildAt(childCount-1)
                removeViewAt(childCount-1)
                currentIndex -= 1
                addView(view, 0)
            }
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
        when (clipOrientation) {
            CLIP_RIGHT -> {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    touchDragButton =
                        !(currentX - event.x > 11.5f.dp || abs(event.y - currentY) > 18.5f.dp || !canTouchDrag)
                    return touchDragButton
                }
            }
            CLIP_LEFT -> {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    touchDragButton =
                        !(!(event.x < dragWidth && event.x > 18.5f.dp) || abs(event.y - currentY) > 18.5f.dp || !canTouchDrag)

                    return touchDragButton
                }
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (clipOrientation) {
            CLIP_RIGHT -> {
                if (onTouchWhenClipRight(event)) return touchDragButton
            }
            CLIP_LEFT -> {
                if (onTouchWhenClipLeft(event)) return touchDragButton
            }
        }
        return false
    }

    private fun onTouchWhenClipRight(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                if (touchDragButton) {
                    currentX = max(mwidth / 3, event.x)

                    currentY = when {
                        event.y > mheight - 29.5f.dp -> {
                            mheight - 29.5f.dp
                        }
                        event.y < 29.5f.dp -> {
                            29.5f.dp
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
                    !(currentX - event.x > 33.2f.dp || abs(event.y - currentY) > 18.5f.dp || !canTouchDrag)
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (touchDragButton) {
                    if (currentX < mwidth / 2) {
                        drawArrow = false
                        touchMoveAnimator.start()
                    } else {
                        if (currentX >= mwidth - 6f.dp) {
                            if (currentIndex > 0) {
                                clipOrientation = CLIP_LEFT
                                changeOrientation=true
                            }
                            dragGenerateAnimator.start()
                        } else {
                            dragReboundAnimator.start()
                        }

                    }
                }
            }
        }
        return false
    }

    private fun onTouchWhenClipLeft(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                if (touchDragButton) {
                    currentX = min(mwidth / 3 * 2, event.x)
                    currentY = when {
                        event.y > mheight - 29.5f.dp -> {
                            mheight - 29.5f.dp
                        }
                        event.y < 29.5f.dp -> {
                            29.5f.dp
                        }
                        else -> {
                            event.y
                        }
                    }
                    drawArrow = currentX <= mwidth / 2
                    invalidate()
                }
            }
            MotionEvent.ACTION_DOWN -> {
                touchDragButton =
                    !(currentX - event.x > 33.2f.dp || abs(event.y - currentY) > 18.5f.dp || !canTouchDrag)
                return touchDragButton
            }
            MotionEvent.ACTION_UP -> {
                if (touchDragButton) {
                    if (currentX > mwidth / 2) {
                        drawArrow = false
                        touchMoveAnimator.start()
                    } else {
                        if (currentX <= 6f.dp) {
                            if (currentIndex<recyclePool.size-1) {
                                clipOrientation = CLIP_RIGHT
                                changeOrientation=true
                            }
                            dragGenerateAnimator.start()
                        } else {
                            dragReboundAnimator.start()
                        }

                    }
                }
            }
        }
        return false
    }


    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        if (childCount > 1) {
            if (child == getChildAt(childCount - 1)) {
                if (clipOrientation == CLIP_RIGHT) {
                    clipRightCanvas(canvas)
                } else {
                    clipLeftCanvas(canvas)
                }
                return super.drawChild(canvas, child, drawingTime)
            }
            if (child == getChildAt(childCount - 2)) {
                child.setBackgroundColor(Color.BLACK)
                return super.drawChild(canvas, child, drawingTime)
            }
        }
        return false
    }

    private fun clipLeftCanvas(canvas: Canvas) {
        canvas.save()
        if (drawArrow) {
            arrowPath.addCircle(
                currentX - 18.5f.dp,
                currentY,
                11.5f.dp,
                Path.Direction.CCW
            )
            arrowPath.moveTo(
                (currentX - 18.5f.dp-2.3f.dp),
                currentY - 6f.dp
            )
            arrowPath.lineTo(
                (currentX - 18.5f.dp+6f.dp),
                currentY
            )
            arrowPath.lineTo(
                (currentX - 18.5f.dp-2.3f.dp),
                currentY + 6f.dp
            )
            canvas.drawPath(arrowPath, arrowPaint)
        }
        arrowPath.reset()
        canvas.restore()

        dragButtonWidth = currentX - 11.5f.dp
        dragButtonHeight = dragButtonWidth / 0.7f
        if (dragButtonWidth < 33.2f.dp) {
            dragButtonHeight /= (dragButtonWidth / 33.2f.dp)
        }
        fringeOffset = (currentX + 11.5f.dp) / dragWidth * 11.5f.dp + moveFringeOffset
        dragTopValuePoint.x = fringeOffset
        dragTopValuePoint.y = currentY - dragButtonHeight

        dragPeakValuePoint.x = currentX + dragToRightOffset
        dragPeakValuePoint.y = currentY


        dragBottomValuePoint.x = fringeOffset
        dragBottomValuePoint.y = currentY + dragButtonHeight

        dragTopControlPoint1.x = dragTopValuePoint.x
        dragTopControlPoint1.y = (dragTopValuePoint.y + dragPeakValuePoint.y) / 2 + 11.5f.dp

        dragTopControlPoint2.x =
            dragPeakValuePoint.x * 0.94f
        dragTopControlPoint2.y = (dragTopValuePoint.y + dragPeakValuePoint.y) / 2

        dragBottomControlPoint1.x =
            dragPeakValuePoint.x * 0.94f
        dragBottomControlPoint1.y = (dragPeakValuePoint.y + dragBottomValuePoint.y) / 2


        dragBottomControlPoint2.x = dragBottomValuePoint.x
        dragBottomControlPoint2.y =
            dragPeakValuePoint.y + (dragPeakValuePoint.y - dragTopValuePoint.y) / 2 - 11.5f.dp


        dragPath.moveTo(0f, 0f)
        dragPath.lineTo(fringeOffset, 0f)
        dragPath.lineTo(fringeOffset, currentY - dragButtonHeight)
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
        dragPath.lineTo(fringeOffset, mheight)
        dragPath.lineTo(0f, mheight)
        canvas.clipPath(dragPath, Region.Op.DIFFERENCE)
        dragPath.reset()
    }

    private fun clipRightCanvas(canvas: Canvas) {
        canvas.save()
        if (drawArrow) {
            arrowPath.addCircle(
                currentX + 18.5f.dp,
                currentY,
                arrowRadius,
                Path.Direction.CCW
            )
            arrowPath.moveTo(
                (currentX + 18.5f.dp+2.3f.dp),
                currentY - 6f.dp
            )
            arrowPath.lineTo(
                (currentX + 18.5f.dp-6f.dp),
                currentY
            )
            arrowPath.lineTo(
                (currentX + 18.5f.dp+2.3f.dp),
                currentY + 6f.dp
            )
            canvas.drawPath(arrowPath, arrowPaint)
        }
        arrowPath.reset()
        canvas.restore()

        dragButtonWidth = mwidth - 11.5f.dp - currentX
        dragButtonHeight = dragButtonWidth / 0.7f
        if (dragButtonWidth < 33.2f.dp) {
            dragButtonHeight /= (dragButtonWidth / 90)
        }
        fringeOffset = (mwidth - currentX - 11.5f.dp) / dragWidth * 30f + moveFringeOffset
        dragTopValuePoint.x = mwidth - fringeOffset
        dragTopValuePoint.y = currentY - dragButtonHeight
        dragPeakValuePoint.x = currentX - dragToLeftOffset
        dragPeakValuePoint.y = currentY
        dragBottomValuePoint.x = mwidth - fringeOffset
        dragBottomValuePoint.y = currentY + dragButtonHeight

        dragTopControlPoint1.x = dragTopValuePoint.x
        dragTopControlPoint1.y = (dragTopValuePoint.y + dragPeakValuePoint.y) / 2 + 11.5f.dp

        dragTopControlPoint2.x =
            dragPeakValuePoint.x + (mwidth - dragPeakValuePoint.x - fringeOffset) * 0.06f
        dragTopControlPoint2.y = (dragTopValuePoint.y + dragPeakValuePoint.y) / 2

        dragBottomControlPoint1.x =
            dragPeakValuePoint.x + (mwidth - dragPeakValuePoint.x - fringeOffset) * 0.06f
        dragBottomControlPoint1.y = (dragPeakValuePoint.y + dragBottomValuePoint.y) / 2


        dragBottomControlPoint2.x = dragBottomValuePoint.x
        dragBottomControlPoint2.y =
            dragPeakValuePoint.y + (dragPeakValuePoint.y - dragTopValuePoint.y) / 2 - 11.5f.dp


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
        currentX = if (clipOrientation == CLIP_RIGHT) mwidth - dragWidth else dragWidth
        currentY = if (dragLocation==0f) mheight/2 else dragLocation
        if(currentY<29.5f.dp){
            currentY=29.5f.dp
        }
        if (currentY>mheight-29.5f.dp){
            currentY=mheight-29.5f.dp
        }
    }

    private fun refreshView() {
        removeAllViews()

        recyclePool.clear()
        mAdapter?.let { adapter ->
            for (i in 0 until adapter.getItemCount()) {
                val viewHolder: ViewHolder? = createViewHolder(i)
                    ?: throw IllegalArgumentException("createViewHolder must not be null")
            }
            for (i in 0 until recyclePool.size) {
                if (i > 1) break
                addView(recyclePool[i].itemView, 0)
            }
            currentIndex = 0
            requestLayout()
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