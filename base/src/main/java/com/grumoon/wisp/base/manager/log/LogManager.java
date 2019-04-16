package com.grumoon.wisp.base.manager.log;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

public class LogManager {

    public static void init() {
        // 初始化Logcat日志
        FormatStrategy androidLogFormatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(true)
                .methodCount(1)
                .tag("wisp")
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(androidLogFormatStrategy) {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return true;
            }
        });


        // 初始化Disk日志
        FormatStrategy diskLogFormatStrategy = CsvFormatStrategy.newBuilder()
                .tag("wisp")
                .logStrategy(new LogDiskStrategy())
                .build();
        Logger.addLogAdapter(new DiskLogAdapter(diskLogFormatStrategy));

        // 开启日志清理
        LogCleaner.getInstance().startCleanLog();
    }
}
