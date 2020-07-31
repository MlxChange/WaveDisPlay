package cn.mlx.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import cn.mlx.widget.WaveDisplayView.AdapterDataObservable
import cn.mlx.widget.WaveDisplayView.ViewHolder


/**
 * Project:WaveDisPlay
 * Created by MLX on 2020/7/30.
 */
abstract class WaveAdapter<T>(val context: Context, mList: List<T>) :
    WaveDisplayView.Adapter<ViewHolder>() {

    protected var mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mData = mutableListOf<T>()

    init {
        mData.clear()
        mData.addAll(mList)
    }

    fun updateData(mlist: List<T>) {
        mData.clear()
        mData.addAll(mlist)
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int = mData.size

    fun getItem(position: Int): T = mData[position]

}
