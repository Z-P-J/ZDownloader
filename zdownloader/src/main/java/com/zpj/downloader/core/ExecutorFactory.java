package com.zpj.downloader.core;

import java.util.concurrent.Executor;

public interface ExecutorFactory<T extends Mission> {

    Executor createExecutor(T mission);

}
