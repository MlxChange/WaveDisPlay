package com.mlx.wavedisplay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import cn.mlx.wavedisplay.R
import com.mlx.widget.WaveDisplayView


class MainActivity : AppCompatActivity() {

    lateinit var waveDisplayView: WaveDisplayView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_main)
        waveDisplayView = findViewById(R.id.wave)
        val mData = mutableListOf<Int>(0,1,2,3,4,5,6)
        val waveAdapter = MyAdapter(this, mData)
        waveDisplayView.setAdapter(waveAdapter)

    }




}