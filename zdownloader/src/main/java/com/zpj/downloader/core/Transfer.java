package com.zpj.downloader.core;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

/**
 * 文件传输接口
 * @param <T> mission
 * @author Z-P-J
 */
public interface Transfer<T extends Mission> {

    /**
     * 文件传输
     * @param mission 下载任务
     * @param block 下载块
     * @return 传输结果
     */
    @WorkerThread
    Result transfer(T mission, @NonNull Block block);

}
