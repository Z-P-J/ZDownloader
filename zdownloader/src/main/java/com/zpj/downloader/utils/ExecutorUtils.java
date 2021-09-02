package com.zpj.downloader.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ExecutorUtils {

    private static final ExecutorService sIOExecutor = Executors.newCachedThreadPool();

    public static void submitIO(Runnable runnable) {
        sIOExecutor.submit(runnable);
    }

}
