package cn.mlx.wavedisplay

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import cn.mlx.widget.WaveAdapter
import cn.mlx.widget.WaveDisplayView

/**
 * Project:WaveDisPlay
 * Created by mlxCh on 2020/7/31.
 */
class MyAdapter(context: Context, mList: MutableList<String>) :
    WaveAdapter<String>(context, mList) {


    private var colors = intArrayOf(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA)

    override fun createViewHolder(parent: ViewGroup, type: Int): WaveDisplayView.ViewHolder {
        var view = mInflater.inflate(R.layout.item, parent, false)
        var viewHolder = WaveDisplayView.ViewHolder(view)
        return viewHolder
    }

    override fun bindViewHolder(holder: WaveDisplayView.ViewHolder, position: Int) {
        val tv = holder.itemView.findViewById<TextView>(R.id.item_text)
        tv.text = getItem(position)
        tv.setBackgroundColor(colors[position])
    }

    override fun getItemViewType(i: Int): Int = 0

}