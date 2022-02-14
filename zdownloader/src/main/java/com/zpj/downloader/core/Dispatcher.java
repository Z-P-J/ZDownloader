package com.zpj.downloader.core;

import com.zpj.downloader.BaseMission;
import com.zpj.downloader.constant.Error;

import java.util.List;
import java.util.concurrent.Executor;

public interface Dispatcher<T extends Mission> {

    Executor createExecutor(T mission);

    boolean isDownloading(T mission);

    boolean isPreparing(T mission);

    boolean isWaiting(T mission);

    boolean remove(T mission);

    boolean wait(T mission);

    T nextMission();

    boolean prepare(T mission);

    boolean enqueue(T mission);

    boolean canRetry(T mission, int code, String msg);

    boolean onError(T mission, int code, String msg);

}
