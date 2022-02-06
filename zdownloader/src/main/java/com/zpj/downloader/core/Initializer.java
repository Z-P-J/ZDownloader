package com.zpj.downloader.core;

import android.support.annotation.WorkerThread;

import com.zpj.downloader.core.impl.Config;

public interface Initializer<T extends Mission> {

    @WorkerThread
    Result initMission(Downloader<T> downloader, T mission);

}
