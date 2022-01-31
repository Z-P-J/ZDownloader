package com.zpj.downloader.core;

import android.support.annotation.WorkerThread;

import com.zpj.downloader.core.impl.Config;

public interface Initializer<T extends Mission> {

    @WorkerThread
    void initMission(Downloader<T> downloader, T mission);

}
