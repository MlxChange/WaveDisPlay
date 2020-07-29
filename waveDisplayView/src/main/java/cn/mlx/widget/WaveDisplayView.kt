package cn.mlx.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import kotlin.math.max

/**
 * Project:NetEasy
 * Created by mlxCh on 2020/7/28.
 */
class WaveDisplayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {


    var controllX = 0f
    var controllY = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var maxwidth = 0
        var maxHeight = 0
        var widthUsed = 0
        var heightUsed = 0
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


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        super.dispatchTouchEvent(ev)
        return true
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE) {
            controllX = event.x * 2
            controllY = event.y
            invalidate()
        }
        return true
    }


    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        if (getChildAt(1) == child) {
            var path = Path()

            path.quadTo(controllX, controllY, 0f, height.toFloat())
            canvas.clipPath(path, Region.Op.DIFFERENCE)

        }
        return super.drawChild(canvas, child, drawingTime)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            getChildAt(i).layout(l, t, getChildAt(i).measuredWidth, getChildAt(i).measuredHeight)
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

}