package com.grumoon.wisp.base.manager.log;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.grumoon.wisp.base.utils.StorageUtils;
import com.orhanobut.logger.LogStrategy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 日志写文件策略 <br/>
 * 每天一个文件，以日期命名，例如 wisp_logs_20170911_创建时间timestamp.csv <br/>
 * 参考 LogDiskStrategy
 */
public class LogDiskStrategy implements LogStrategy {

    private static final String DEFAULT_FILE_PREFIX = "wisp";

    private final Handler handler;

    public LogDiskStrategy() {
        this(null);
    }

    public LogDiskStrategy(String logFolderPath) {
        this(logFolderPath, null);
    }

    public LogDiskStrategy(String logFolderPath, String logFilePrefix) {

        if (TextUtils.isEmpty(logFolderPath)) {
            logFolderPath = StorageUtils.getLogDir();
        }

        if (TextUtils.isEmpty(logFilePrefix)) {
            logFilePrefix = DEFAULT_FILE_PREFIX;
        }

        HandlerThread ht = new HandlerThread("AndroidFileLogger." + logFolderPath);
        ht.start();
        this.handler = new WriteHandler(ht.getLooper(), logFolderPath, logFilePrefix);
    }

    @Override
    public void log(int level, String tag, String message) {
        // do nothing on the calling thread, simply pass the tag/msg to the background thread
        handler.sendMessage(handler.obtainMessage(level, message));
    }

    static class WriteHandler extends Handler {

        private final String mLogFolderPath;
        private final String mLogFilePrefix;

        private final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

        WriteHandler(Looper looper, String logFolderPath, String logFilePrefix) {
            super(looper);
            this.mLogFolderPath = logFolderPath;
            this.mLogFilePrefix = logFilePrefix;
        }

        @SuppressWarnings("checkstyle:emptyblock")
        @Override
        public void handleMessage(Message msg) {
            String content = (String) msg.obj;

            FileWriter fileWriter = null;
            File logFile = getLogFile(mLogFolderPath, mLogFilePrefix);

            try {
                fileWriter = new FileWriter(logFile, true);

                writeLog(fileWriter, content);

                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileWriter != null) {
                    try {
                        fileWriter.close();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        /**
         * This is always called on a single background thread.
         * Implementing classes must ONLY write to the fileWriter and nothing more.
         * The abstract class takes care of everything else including close the stream and catching IOException
         *
         * @param fileWriter an instance of FileWriter already initialised to the correct file
         */
        private void writeLog(FileWriter fileWriter, String content) throws IOException {
            fileWriter.append(content);
        }

        private File getLogFile(String folderName, String prefix) {

            File folder = new File(folderName);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // 当前时间 yyyyMMdd
            Calendar now = Calendar.getInstance();
            now.set(Calendar.HOUR, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);
            now.set(Calendar.HOUR_OF_DAY, 0);

            String dateString = formatter.format(now.getTime());


            long nowDateTimestamp = now.getTimeInMillis();

            // android 无法获取创建时间，所有将创建时间时间戳，写入文件名
            File logFile = new File(folder, String.format("%s_%s_%d.csv", prefix, dateString, nowDateTimestamp));

            return logFile;
        }
    }
}
