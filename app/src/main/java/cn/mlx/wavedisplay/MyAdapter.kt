package cn.mlx.wavedisplay

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import cn.mlx.widget.WaveAdapter
import cn.mlx.widget.WaveDisplayView
import coil.api.load

/**
 * Project:WaveDisPlay
 * Created by mlxCh on 2020/7/31.
 */
class MyAdapter(context: Context, mList: MutableList<Int>) :
    WaveAdapter<Int>(context, mList) {

    val imgs = intArrayOf(
        R.mipmap.img0,
        R.mipmap.img1,
        R.mipmap.img2,
        R.mipmap.img3,
        R.mipmap.img4,
        R.mipmap.img5,
        R.mipmap.img6
    )

    override fun createViewHolder(parent: ViewGroup, type: Int): WaveDisplayView.ViewHolder {
        var view = mInflater.inflate(R.layout.item, parent, false)
        return WaveDisplayView.ViewHolder(view)
    }

    override fun bindViewHolder(holder: WaveDisplayView.ViewHolder, position: Int) {
        val img = holder.itemView.findViewById<ImageView>(R.id.item_img)
        img.scaleType=ImageView.ScaleType.FIT_XY
        img.load(imgs[position]){
            crossfade(true)
        }
    }

    override fun getItemViewType(i: Int): Int = 0

}