package com.zpj.downloader.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPool {


    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private static final class Holder {
        private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    }


    public static void execute(Runnable runnable) {
        EXECUTOR.execute(runnable);
    }

    public static void post(Runnable runnable) {
        Holder.HANDLER.post(runnable);
    }

    public static void postDelayed(Runnable r, long delayMillis) {
        Holder.HANDLER.postDelayed(r, delayMillis);
    }

    public static Future<?> submit(Runnable runnable) {
        return EXECUTOR.submit(runnable);
    }

}
