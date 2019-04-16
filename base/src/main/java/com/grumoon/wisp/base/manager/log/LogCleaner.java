package com.grumoon.wisp.base.manager.log;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.grumoon.wisp.base.utils.StorageUtils;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FilenameFilter;

public class LogCleaner {
    private final static String TAG = "LogCleaner";


    /**
     * 创建时间，最早有效时间 2010/1/1 0:0:0
     * 早于这个时间，认为无效
     */
    private static final long MIN_VALID_TIMESTAMP = 1262275200000L;

    /**
     * 日志清理轮询时间
     */
    private static final int CLEAN_INTERVAL = 6 * 60 * 60 * 1000;

    private static final int CLEAN_KEEP_DAYS = 7;

    private static volatile LogCleaner mInstance = null;

    private Handler mHandler;

    public static LogCleaner getInstance() {
        if (mInstance == null) {
            synchronized (LogCleaner.class) {
                if (mInstance == null) {
                    mInstance = new LogCleaner();
                }
            }
        }
        return mInstance;
    }

    private LogCleaner() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    private void cleanLog(String logFolderPath) {
        try {
            if (TextUtils.isEmpty(logFolderPath)) {
                Logger.t(TAG).e("cleanLog logFolderPath is null");
                return;
            }

            if (!(new File(logFolderPath).exists())) {
                Logger.t(TAG).e("cleanLog %s not exist", logFolderPath);
                return;
            }

            long timeNow = System.currentTimeMillis();
            final long timeBegin = timeNow - CLEAN_KEEP_DAYS * 24 * 3600 * 1000;


            File[] toDeleteFiles = new File(logFolderPath).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    long createTime = getCreateTimeFromFileName(name);
                    // 为了不影响清理机制的兼容性，对于不能获取创建时间的，暂时保留，不删除
                    if (createTime == -1) {
                        return false;
                    }
                    return createTime < timeBegin;
                }
            });

            if (toDeleteFiles != null) {
                for (File file : toDeleteFiles) {
                    Logger.t(TAG).d("cleanLog delete file = " + file.getName());
                    file.delete();
                }
            }
        } catch (Exception e) {
            Logger.t(TAG).e("cleanLog e = " + e);
        }
    }


    private Runnable mCleanTask = new Runnable() {
        @Override
        public void run() {
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        cleanLog(StorageUtils.getLogDir());
                    }
                }).start();
            } catch (Exception e) {
                Logger.t(TAG).e("mCleanTask e = " + e);
            }

            if (mHandler != null) {
                mHandler.removeCallbacks(mCleanTask);
                mHandler.postDelayed(this, CLEAN_INTERVAL);
            }
        }
    };


    public void startCleanLog() {
        mHandler.removeCallbacks(mCleanTask);
        mHandler.post(mCleanTask);
    }

    public void stopCleanLog() {
        mHandler.removeCallbacks(mCleanTask);
    }


    /**
     * 通过文件名获取文件创建时间
     *
     * @param fileName
     * @return -1 表示获取失败
     */
    public static long getCreateTimeFromFileName(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return -1;
        }

        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return -1;
        }

        int lastUnderlineIndex = fileName.lastIndexOf("_");
        if (lastUnderlineIndex == -1) {
            return -1;
        }

        if (lastUnderlineIndex >= lastDotIndex) {
            return -1;
        }

        try {
            String createTimeStr = fileName.substring(lastUnderlineIndex + 1, lastDotIndex);
            long createTimestamp = Long.parseLong(createTimeStr);
            // 早于最早有效时间，认为无效数据
            if (createTimestamp < MIN_VALID_TIMESTAMP) {
                return -1;
            }
            return createTimestamp;
        } catch (Exception e) {
            return -1;
        }
    }
}
