package com.zpj.downloader.impl;

import com.zpj.downloader.core.Mission;
import com.zpj.downloader.core.MissionExecutor;
import com.zpj.downloader.core.MissionExecutorFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MissionExecutorFactoryImpl<T extends Mission> implements MissionExecutorFactory<T> {

    @Override
    public MissionExecutor<T> createExecutor(T mission) {
        return new MissionExecutorImpl<>(mission);
    }

}
