package com.zpj.downloader.core;

import com.zpj.downloader.BaseMission;

import java.io.File;

/**
 * 下载任务序列化/反序列化接口
 */
public interface Serializer<T extends Mission> {

    T readMission(final File file, final Class<T> clazz);

    void writeMission(final T mission);

}
