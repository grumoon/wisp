package com.grumoon.wisp.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Button
import com.grumoon.wisp.R
import com.grumoon.wisp.base.manager.cast.CastManager
import com.orhanobut.logger.Logger


class MainActivity : BaseActivity() {
    companion object {
        private const val TAG = "MainActivity"

        /**
         * 申请投屏权限
         */
        private const val REQUEST_MEDIA_PROJECTION_RECORD = 10001
        private const val REQUEST_MEDIA_PROJECTION_CAPTURE = 10002
    }

    private var mBtnRecordScreen: Button? = null
    private var mBtnScreenshot: Button? = null
    private var mRecordingFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        mBtnRecordScreen = findViewById(R.id.btn_record_screen)
        mBtnRecordScreen?.setOnClickListener {
            if (mRecordingFlag) {
                stopRecord()
            } else {
                if (!CastManager.getInstance().checkCapturePermission()) {
                    val reqIntent = (getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager).createScreenCaptureIntent()
                    startActivityForResult(reqIntent, REQUEST_MEDIA_PROJECTION_RECORD)
                } else {
                    startRecord()
                }
            }
            mRecordingFlag = !mRecordingFlag;
            mBtnRecordScreen?.text = if (mRecordingFlag) "停止录屏" else "开始录屏"
        }

        mBtnScreenshot = findViewById(R.id.btn_screenshot)
        mBtnScreenshot?.setOnClickListener {
            if (!CastManager.getInstance().checkCapturePermission()) {
                val reqIntent = (getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager).createScreenCaptureIntent()
                startActivityForResult(reqIntent, REQUEST_MEDIA_PROJECTION_CAPTURE)
            } else {
                screenshot()
            }
        }

        CastManager.getInstance().setCastManagerListener(object : CastManager.CastManagerListener {
            override fun onScreenshotResult(retCode: Int, imageSavePath: String?) {
                Logger.t(TAG).d("onScreenshotResult retCode = $retCode | imageSavePath = $imageSavePath")
                if (retCode == 0) {
                    showToast("截屏完成，图片存储地址 $imageSavePath")
                } else {
                    showToast("截屏失败，错误码 $retCode")
                }
            }

            override fun onRecordScreenResult(retCode: Int, videoSavePath: String?) {
                Logger.t(TAG).d("onRecordScreenResult retCode = $retCode | videoSavePath = $videoSavePath")
                if (retCode == 0) {
                    showToast("录屏完成，视频存储地址 $videoSavePath")
                } else {
                    showToast("录屏失败，错误码 $retCode")
                }
            }
        })
    }


    private fun startRecord() {
        showToast("开始录屏...")
        CastManager.getInstance().startRecord()
    }

    private fun stopRecord() {
        CastManager.getInstance().stopRecord()
    }

    private fun screenshot() {
        showToast("10秒钟后开始截屏")
        mBaseHandler?.postDelayed({
            CastManager.getInstance().screenshot()
        }, 10 * 1000)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_MEDIA_PROJECTION_RECORD || requestCode == REQUEST_MEDIA_PROJECTION_CAPTURE) {
            if (resultCode != Activity.RESULT_OK) {
                Logger.t(TAG).e("user cancel")
                showToast("获取投屏权限失败")
                return
            }

            CastManager.getInstance().setCapturePermission(resultCode, data)

            if (requestCode == REQUEST_MEDIA_PROJECTION_RECORD) {
                startRecord()
            } else {
                screenshot()
            }
        }
    }
}
