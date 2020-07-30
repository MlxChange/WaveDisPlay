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
abstract class WaveAdapter <T>(val context: Context, mlist:List<T>): WaveDisplayView.Adapter<ViewHolder>() {

    private var mInflater:LayoutInflater = LayoutInflater.from(context)
    private var mdata= mutableListOf<T>()
    init {
        mdata.clear()
        mdata.addAll(mlist)
    }

    fun updateData(mlist:List<T>){
        mdata.clear()
        mdata.addAll(mlist)
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int = mdata.size

    fun getItem(position: Int): T = mdata[position]

}
