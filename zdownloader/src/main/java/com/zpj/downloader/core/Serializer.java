package com.zpj.downloader.core;

import com.zpj.downloader.BaseMission;

import java.io.File;

/**
 * 下载任务序列化/反序列化接口
 */
public interface Serializer {

    BaseMission<?> readMission(File file, Class<? extends BaseMission<?>> clazz);

    void writeMission(BaseMission<?> mission);

}
