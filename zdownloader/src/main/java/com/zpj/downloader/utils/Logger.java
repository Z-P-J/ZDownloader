package com.zpj.downloader.utils;

import android.util.Log;

public class Logger {

    private static final String PREFIX = "ZDownloader_";

    public static void d(String tag, String msg) {
        Log.d(PREFIX + tag, msg);
    }

    public static void e(String tag, String msg) {
        Log.d(PREFIX + tag, msg);
    }

}
