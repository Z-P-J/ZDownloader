package com.zpj.downloader.impl;

import com.zpj.downloader.core.BaseDownloader;
import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.MissionExecutor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MissionExecutorImpl<T extends Mission> implements MissionExecutor<T> {

    private final ThreadPoolExecutor mExecutor;

    public MissionExecutorImpl(T mission) {
        int nThreads = mission.getConfig().getThreadCount();
        this.mExecutor = new ThreadPoolExecutor(nThreads, nThreads,
                10L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
        this.mExecutor.allowCoreThreadTimeOut(true);;
    }

    @Override
    public void execute(BaseDownloader.BlockTask<T> block) {
        mExecutor.execute(block);
    }

    @Override
    public void shutdown() {
        mExecutor.shutdownNow();
    }
}
