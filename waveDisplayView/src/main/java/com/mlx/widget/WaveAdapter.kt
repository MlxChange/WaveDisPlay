package com.mlx.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.mlx.widget.WaveDisplayView.AdapterDataObservable
import com.mlx.widget.WaveDisplayView.ViewHolder


/**
 * Project:WaveDisPlay
 * Created by MLX on 2020/7/30.
 */
abstract class WaveAdapter(val context: Context) :
    WaveDisplayView.Adapter<ViewHolder>() {
    protected var mInflater: LayoutInflater = LayoutInflater.from(context)
}
