package com.grumoon.wisp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ListView
import com.grumoon.wisp.R


class MainActivity : BaseActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private var mModuleAdapter: BaseAdapter? = null
    private var mLvModule: ListView? = null

    private val mModuleNameList = arrayOf("摄像头", "屏幕采集")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mModuleAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mModuleNameList)

        mLvModule = findViewById(R.id.lv_module)
        mLvModule?.adapter = mModuleAdapter
        mLvModule?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            when (position) {
                0 -> startActivity(Intent(this@MainActivity, CameraActivity::class.java))
                1 -> startActivity(Intent(this@MainActivity, CastActivity::class.java))
            }
        }


    }

}
