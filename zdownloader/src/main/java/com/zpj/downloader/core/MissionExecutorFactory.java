package com.zpj.downloader.core;

import java.util.concurrent.ExecutorService;

public interface MissionExecutorFactory<T extends Mission> {

    MissionExecutor<T> createExecutor(T mission);

}
