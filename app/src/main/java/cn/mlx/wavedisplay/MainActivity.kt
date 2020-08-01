package cn.mlx.wavedisplay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import cn.mlx.widget.WaveDisplayView

class MainActivity : AppCompatActivity() {

    lateinit var waveDisplayView: WaveDisplayView<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_main)
        waveDisplayView = findViewById(R.id.wave)
        val mData = mutableListOf<Int>()
        for (i in 0..6) {
            mData.add(i)
        }
        val waveAdapter = MyAdapter(this, mData)
        waveDisplayView.setAdapter(waveAdapter)

    }




}