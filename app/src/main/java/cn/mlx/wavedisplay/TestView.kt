package cn.mlx.wavedisplay

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withSave

/**
 * Project:WaveDisPlay
 * Created by MLX on 2020/7/28.
 */
class TestView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var path = Path()
    var paint = Paint()

    var mwidth = 0
    var mheight = 0
    var currentX=340f
    var currentY=1200f
    var angle=1f
    init {
        paint.color = Color.RED
        paint.isAntiAlias = true
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.style = Paint.Style.STROKE
        paint.strokeWidth=10f

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        angle=(currentX-100)/140*0.5f
        val pathEffect: PathEffect = CornerPathEffect(200f*angle)
        paint.pathEffect = pathEffect
        path.moveTo(100f, 0f)
        var length=(currentX-100)/1.4f

        var shangY=currentY-length
        path.lineTo(100f,shangY)
        path.lineTo(currentX,shangY+length)
        path.lineTo(100f,shangY+length*2)
        path.lineTo(100f,mheight.toFloat())
        canvas.drawPath(path, paint)
        path.reset()
        canvas.restore()
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action==MotionEvent.ACTION_MOVE){
            currentX=event.x
            currentY=event.y
            invalidate()
        }
        return true
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mwidth = w
        mheight = h
    }

}