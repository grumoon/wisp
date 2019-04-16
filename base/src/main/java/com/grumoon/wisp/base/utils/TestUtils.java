package com.grumoon.wisp.base.utils;

public class TestUtils {

    static {
        System.loadLibrary("native-lib");
    }

    public native static String stringFromJNI();
}
