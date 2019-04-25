package com.grumoon.wisp.base.utils;

import android.os.Environment;

import java.io.File;

public class StorageUtils {

    public static final String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();

    public static final String WISP_ROOT = SDCARD_ROOT + File.separatorChar + "wisp";

    private static final String TEMP = WISP_ROOT + File.separatorChar + "temp";

    private static final String CONFIG = WISP_ROOT + File.separatorChar + "config";

    private static final String LOG = WISP_ROOT + File.separatorChar + "log";

    private static final String CAPTURE = WISP_ROOT + File.separatorChar + "capture";

    private static final String CAMERA = WISP_ROOT + File.separatorChar + "camera";

    public static final String getTempDir() {
        File path = new File(TEMP);
        if (!path.exists()) {
            path.mkdirs();
        }
        return TEMP;
    }

    public static final String getConfigDir() {
        File path = new File(CONFIG);
        if (!path.exists()) {
            path.mkdirs();
        }
        return CONFIG;
    }

    public static final String getLogDir() {
        File path = new File(LOG);
        if (!path.exists()) {
            path.mkdirs();
        }
        return LOG;
    }

    public static final String getCaptureDir() {
        File path = new File(CAPTURE);
        if (!path.exists()) {
            path.mkdirs();
        }
        return CAPTURE;
    }

    public static final String getCameraDir() {
        File path = new File(CAMERA);
        if (!path.exists()) {
            path.mkdirs();
        }
        return CAMERA;
    }
}
