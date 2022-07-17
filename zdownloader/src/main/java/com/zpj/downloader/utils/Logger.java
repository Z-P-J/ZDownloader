package com.zpj.downloader.utils;

import android.util.Log;

import com.zpj.downloader.BuildConfig;

/**
 * 日志工具类
 * @author Z-P-J
 */
public class Logger {

    private static final String PREFIX = "ZDownloader.";

    private static boolean DEBUG = BuildConfig.DEBUG;

    public static void init(boolean debug) {
        DEBUG = debug;
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(PREFIX + tag, msg);
        }
    }

    public static void i(String tag, String format, Object...args) {
        if (DEBUG) {
            i(tag, String.format(format, args));
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(PREFIX + tag, msg);
        }
    }

    public static void d(String tag, String format, Object...args) {
        if (DEBUG) {
            d(tag, String.format(format, args));
        }
    }

    public static void w(String tag, String msg) {
        Log.w(PREFIX + tag, msg);
    }

    public static void w(String tag, String format, Object...args) {
        if (DEBUG) {
            w(tag, String.format(format, args));
        }
    }

    public static void e(String tag, String msg) {
        Log.e(PREFIX + tag, msg);
    }

    public static void e(String tag, String msg, Throwable e) {
        Log.e(PREFIX + tag, msg, e);
    }

}
