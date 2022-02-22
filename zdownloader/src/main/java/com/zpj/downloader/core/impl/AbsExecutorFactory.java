package com.zpj.downloader.core.impl;

import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.ExecutorFactory;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AbsExecutorFactory<T extends Mission> implements ExecutorFactory<T> {

    @Override
    public ExecutorService createExecutor(T mission) {
        int nThreads = mission.getConfig().getThreadCount();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(nThreads, nThreads,
                10L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

}
