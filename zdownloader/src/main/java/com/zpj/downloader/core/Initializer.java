package com.zpj.downloader.core;

import android.support.annotation.WorkerThread;

public interface Initializer<T extends Mission> {

    @WorkerThread
    Result initMission(Downloader<T> downloader, T mission);

}
