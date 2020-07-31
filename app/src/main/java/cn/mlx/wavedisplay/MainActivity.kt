package cn.mlx.wavedisplay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import cn.mlx.widget.WaveAdapter
import cn.mlx.widget.WaveDisplayView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var waveDisplayView: WaveDisplayView<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        waveDisplayView = findViewById(R.id.wave)
        var datas = mutableListOf<String>()
        for (i in 0..5) {
            datas.add("${i}")
        }
        val waveAdapter = MyAdapter(this, datas)
        waveDisplayView.setAdapter(waveAdapter)

    }




}