package com.grumoon.wisp.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast


open class BaseActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BaseActivity"
    }

    /**
     * 构造一个主线程的Handler，用于投递消息
     */
    protected var mBaseHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mBaseHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                baseHandleMessage(msg)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                this.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        mBaseHandler?.removeCallbacksAndMessages(null)
        mBaseHandler = null
    }


    /**
     * 子类重写此方法，进行MSG处理
     *
     * @param msg
     */
    fun baseHandleMessage(msg: Message) {

    }


    fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
}