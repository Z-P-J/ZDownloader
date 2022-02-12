package com.zpj.downloader.core;

import android.support.annotation.WorkerThread;

public interface Transfer<T extends Mission> {

    @WorkerThread
    Result transfer(T mission, Block block);

}
