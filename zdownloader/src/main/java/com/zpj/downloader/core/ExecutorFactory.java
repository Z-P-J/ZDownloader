package com.zpj.downloader.core;

import java.util.concurrent.ExecutorService;

public interface ExecutorFactory<T extends Mission> {

    ExecutorService createExecutor(T mission);

}
