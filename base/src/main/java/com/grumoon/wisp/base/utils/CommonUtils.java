package com.grumoon.wisp.base.utils;

import android.graphics.Bitmap;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;

public class CommonUtils {

    private static final String TAG = "CommonUtils";

    public static void saveBitmap(Bitmap bitmap, String saveFilePath) {
        try {
            File f = new File(saveFilePath);
            if (f.exists()) {
                f.delete();
            }

            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            Logger.t(TAG).e("saveBitmap e = " + e);
        }

    }
}
