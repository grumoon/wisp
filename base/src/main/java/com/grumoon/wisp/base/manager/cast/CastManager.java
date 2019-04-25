package com.grumoon.wisp.base.manager.cast;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.grumoon.wisp.base.utils.CommonUtils;
import com.grumoon.wisp.base.utils.StorageUtils;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.nio.ByteBuffer;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;

public class CastManager {

    public interface CastManagerListener {
        void onScreenshotResult(int retCode, String imageSavePath);

        void onRecordScreenResult(int retCode, String videoSavePath);
    }

    private static final String TAG = "CastManager";


    private static volatile CastManager mInstance = null;
    private boolean mInitFlag = false;
    private Context mContext;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;

    private int mScreenWidth = 0;
    private int mScreenHeight = 0;
    private int mScreenDensity = 0;


    private int mPermissionCode = 0;
    private Intent mPermissionData = null;

    private Handler mBgHandler;

    private MediaRecorder mMediaRecorder;

    private volatile boolean mScreenshotFlag = false;
    private volatile boolean mScreenRecordFlag = false;

    private VirtualDisplay mVirtualDisplay;

    private String mCurrentScreenRecordFilePath;

    private CastManagerListener mCastManagerListener = null;

    private CastManager() {

    }

    public static CastManager getInstance() {
        if (mInstance == null) {
            synchronized (CastManager.class) {
                if (mInstance == null) {
                    mInstance = new CastManager();
                }
            }
        }

        return mInstance;
    }

    public void init(Context context) {
        if (mInitFlag) {
            return;
        }

        if (context == null) {
            return;
        }


        HandlerThread bgThread = new HandlerThread("CastManager_BgThread");
        bgThread.start();
        Looper bgLooper = bgThread.getLooper();
        mBgHandler = new Handler(bgLooper);

        mContext = context.getApplicationContext();

        mMediaProjectionManager = (MediaProjectionManager) mContext.getSystemService(MEDIA_PROJECTION_SERVICE);

        mScreenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        mScreenDensity = mContext.getResources().getDisplayMetrics().densityDpi;

        mMediaRecorder = new MediaRecorder();

        mInitFlag = true;

    }

    public void setCastManagerListener(CastManagerListener castManagerListener) {
        this.mCastManagerListener = castManagerListener;
    }

    public void setCapturePermission(int permissionCode, Intent permissionData) {
        Logger.t(TAG).d("setCapturePermission permissionCode = %d | permissionData = %s", permissionCode, permissionData);
        mPermissionCode = permissionCode;
        mPermissionData = permissionData;
    }

    public boolean checkCapturePermission() {
        return mPermissionCode != 0 && mPermissionData != null;
    }

    private void setUpMediaProjection() {
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mPermissionCode, mPermissionData);
    }

    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    private void prepareRecorder() {
        mCurrentScreenRecordFilePath = StorageUtils.getCaptureDir() + File.separatorChar + System.currentTimeMillis() + ".mp4";
        File file = new File(mCurrentScreenRecordFilePath);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(file.getAbsolutePath());
        mMediaRecorder.setVideoSize(mScreenWidth, mScreenHeight);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
        mMediaRecorder.setVideoFrameRate(30);
        try {
            mMediaRecorder.prepare();
        } catch (Exception e) {
            Logger.t(TAG).e("prepareRecorder e = " + e);
        }
    }


    public void startRecord() {
        if (!mInitFlag) {
            Logger.t(TAG).d("startRecord not init");
            return;
        }

        if (!checkCapturePermission()) {
            Logger.t(TAG).d("startRecord permission info not set");
            return;
        }

        if (mScreenRecordFlag) {
            Logger.t(TAG).d("startRecord repetitive operation");
            return;
        }

        mScreenRecordFlag = true;

        try {
            prepareRecorder();
            setUpMediaProjection();
            mVirtualDisplay = mMediaProjection.createVirtualDisplay(
                    "MainScreen",
                    mScreenWidth,
                    mScreenHeight,
                    mScreenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mMediaRecorder.getSurface(),
                    null, null);

            mMediaRecorder.start();
        } catch (Exception e) {
            Logger.t(TAG).e("startRecord e = " + e);
            if (mCastManagerListener != null) {
                mCastManagerListener.onRecordScreenResult(-1, null);
            }
            mScreenRecordFlag = false;
        }
    }

    public void stopRecord() {
        if (!mInitFlag) {
            Logger.t(TAG).d("stopRecord not init");
            return;
        }

        if (!checkCapturePermission()) {
            Logger.t(TAG).d("stopRecord permission info not set");
            return;
        }

        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mVirtualDisplay.release();
        tearDownMediaProjection();

        if (mCastManagerListener != null) {
            mCastManagerListener.onRecordScreenResult(0, mCurrentScreenRecordFilePath);
        }
        mScreenRecordFlag = false;
    }

    public void screenshot() {
        if (!mInitFlag) {
            Logger.t(TAG).d("screenshot not init");
            return;
        }

        if (!checkCapturePermission()) {
            Logger.t(TAG).d("screenshot permission info not set");
            return;
        }

        if (mScreenshotFlag) {
            Logger.t(TAG).d("screenshot repetitive operation");
            return;
        }

        mScreenshotFlag = true;

        setUpMediaProjection();

        ImageReader imageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 2);

        mMediaProjection.createVirtualDisplay(
                "screen-mirror",
                mScreenWidth,
                mScreenHeight,
                mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(),
                null,
                null);

        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {

                try (Image image = reader.acquireLatestImage()) {
                    if (image != null) {
                        final Image.Plane[] planes = image.getPlanes();
                        if (planes.length > 0) {
                            int imageWidth = image.getWidth();
                            int imageHeight = image.getHeight();
                            final ByteBuffer buffer = planes[0].getBuffer();
                            int pixelStride = planes[0].getPixelStride();
                            int rowStride = planes[0].getRowStride();
                            int rowPadding = rowStride - pixelStride * imageWidth;


                            // create bitmap
                            Bitmap bmp = Bitmap.createBitmap(imageWidth + rowPadding / pixelStride, imageHeight, Bitmap.Config.ARGB_8888);
                            bmp.copyPixelsFromBuffer(buffer);

                            Bitmap croppedBitmap = Bitmap.createBitmap(bmp, 0, 0, mScreenWidth, mScreenHeight);


                            String saveImageFilePath = StorageUtils.getCaptureDir() + File.separatorChar + System.currentTimeMillis() + ".png";

                            CommonUtils.saveBitmap(croppedBitmap, saveImageFilePath);//保存图片

                            if (mCastManagerListener != null) {
                                mCastManagerListener.onScreenshotResult(0, saveImageFilePath);
                            }

                            if (croppedBitmap != null) {
                                croppedBitmap.recycle();
                            }
                            if (bmp != null) {
                                bmp.recycle();
                            }
                        }
                    } else {
                        Logger.t(TAG).e("onImageAvailable image is null");
                        if (mCastManagerListener != null) {
                            mCastManagerListener.onScreenshotResult(-1, null);
                        }
                    }
                } catch (Exception e) {
                    if (mCastManagerListener != null) {
                        mCastManagerListener.onScreenshotResult(-2, null);
                    }
                    Logger.t(TAG).e("onImageAvailable e = " + e);
                } finally {
                    if (reader != null) {
                        reader.close();
                        reader.setOnImageAvailableListener(null, null);
                    }

                    tearDownMediaProjection();

                    mScreenshotFlag = false;
                }

            }
        }, mBgHandler);

    }


}
