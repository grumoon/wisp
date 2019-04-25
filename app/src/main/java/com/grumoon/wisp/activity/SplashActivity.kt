package com.grumoon.wisp.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.widget.Toast
import com.grumoon.wisp.R
import com.orhanobut.logger.Logger
import java.util.*

open class SplashActivity : BaseActivity() {

    companion object {

        private const val TAG = "SplashActivity"

        private const val PERMISSION_REQUEST_CODE = 10001

        /**
         * 闪屏最少显示时间
         */
        private const val SPLASH_SHOW_MIN_TIME = 2000
    }


    private val mPermissionMap = HashMap<String, String>()
    private val mPermissionToRequest = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        mPermissionMap[Manifest.permission.WRITE_EXTERNAL_STORAGE] = "读写SD卡"
        mPermissionMap[Manifest.permission.RECORD_AUDIO] = "录音"

        // 申请权限
        if (Build.VERSION.SDK_INT >= 23) {
            mPermissionToRequest.clear()

            var shouldShowDialog = false
            var permissionDialogStr = ""

            for ((key, value) in mPermissionMap) {
                if (ContextCompat.checkSelfPermission(this@SplashActivity, key) != PackageManager.PERMISSION_GRANTED) {
                    mPermissionToRequest.add(key)
                    permissionDialogStr += "$value，"
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this@SplashActivity, key)) {
                        shouldShowDialog = true
                    }
                }
            }

            if (permissionDialogStr.isNotEmpty()) {
                permissionDialogStr = permissionDialogStr.substring(0, permissionDialogStr.length - 1)
            }

            if (!mPermissionToRequest.isEmpty()) {
                val names = mPermissionToRequest.toTypedArray()
                if (shouldShowDialog) {
                    showExplanation("申请权限", "运行系统需要申请 $permissionDialogStr 相关权限", names, PERMISSION_REQUEST_CODE)
                } else {
                    ActivityCompat.requestPermissions(this@SplashActivity, names, PERMISSION_REQUEST_CODE)
                }
            } else {
                init()
            }
        } else {
            init()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                var allPermissionGranted = true
                var permissionNotGrantedStr = ""
                if (permissions.isNotEmpty() && grantResults.isNotEmpty() && permissions.size == grantResults.size) {
                    for (i in grantResults.indices) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            allPermissionGranted = false
                            permissionNotGrantedStr += mPermissionMap[permissions[i]] + "，"
                        }
                    }
                } else {
                    allPermissionGranted = false
                }

                if (permissionNotGrantedStr.isNotEmpty()) {
                    permissionNotGrantedStr = permissionNotGrantedStr.substring(0, permissionNotGrantedStr.length - 1)
                }

                if (allPermissionGranted) {
                    init()
                } else {
                    Toast.makeText(this, "$permissionNotGrantedStr 权限申请失败，请在应用设置中允许相关权限。", Toast.LENGTH_SHORT).show()
                    mBaseHandler?.postDelayed({ finish() }, 3000)
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    /**
     * 申请权限显示说明
     *
     * @param title
     * @param message
     * @param permissions
     * @param permissionRequestCode
     */
    private fun showExplanation(title: String,
                                message: String,
                                permissions: Array<String>,
                                permissionRequestCode: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { dialog, id -> ActivityCompat.requestPermissions(this@SplashActivity, permissions, permissionRequestCode) }
        builder.create().show()
    }


    private fun init() {
        mBaseHandler?.postDelayed({
            jump()
        }, SPLASH_SHOW_MIN_TIME.toLong())
    }


    private fun jump() {
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        Logger.t(TAG).d("onBackPressed mCurrentState")
        return
    }
}
