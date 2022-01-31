package com.zpj.downloader.core;

import com.zpj.downloader.BaseMission;
import com.zpj.downloader.constant.Error;

public interface Dispatcher<T extends Mission> {

    void enqueue(T mission);

    void pause(T mission);

    void delete(T mission);

    void notifyError(T mission, final Error e);

    void notifyStatus(final T mission, final int status);

}
